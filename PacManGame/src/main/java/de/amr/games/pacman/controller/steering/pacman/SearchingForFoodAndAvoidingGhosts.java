package de.amr.games.pacman.controller.steering.pacman;

import static de.amr.games.pacman.controller.creatures.ghost.GhostState.CHASING;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.FRIGHTENED;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.SCATTERING;
import static java.util.Comparator.comparingInt;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import de.amr.games.pacman.controller.creatures.Folks;
import de.amr.games.pacman.controller.creatures.ghost.Ghost;
import de.amr.games.pacman.controller.creatures.pacman.PacMan;
import de.amr.games.pacman.controller.steering.api.PathProvidingSteering;
import de.amr.games.pacman.model.world.api.Direction;
import de.amr.games.pacman.model.world.api.BonusFood;
import de.amr.games.pacman.model.world.api.MobileLifeform;
import de.amr.games.pacman.model.world.api.Tile;
import de.amr.games.pacman.model.world.api.World;
import de.amr.games.pacman.model.world.arcade.Pellet;
import de.amr.games.pacman.model.world.core.WorldGraph;
import de.amr.games.pacman.model.world.core.WorldGraph.PathFinder;

/**
 * Steering used by PacMan in demo mode.
 * 
 * @author Armin Reichert
 */
public class SearchingForFoodAndAvoidingGhosts implements PathProvidingSteering<PacMan> {

	static class Target {
		Direction dir;
		Tile tile;

		Target(Direction dir) {
			this.dir = dir;
		}
	}

	private final World world;
	private final Folks folks;
	private final PacMan me;
	private final WorldGraph graph;
	private Tile target;

	public SearchingForFoodAndAvoidingGhosts(Folks folks) {
		this.folks = folks;
		world = folks.world;
		me = folks.pacMan;
		graph = new WorldGraph(folks.world);
		graph.setPathFinder(PathFinder.ASTAR);
	}

	@Override
	public void steer(PacMan pacMan) {
		if (!pacMan.enteredNewTile() && pacMan.canCrossBorderTo(pacMan.moveDir()) || pacMan.isTeleporting()) {
			return;
		}
		boolean acted = avoidTouchingGhostAhead() || avoidOncomingGhost() || chaseFrightenedGhost(10);
		if (!acted) {
			searchFood();
		}
	}

	@Override
	public boolean requiresGridAlignment() {
		return true;
	}

	@Override
	public boolean isPathComputationEnabled() {
		return target != null;
	}

	@Override
	public void setPathComputationEnabled(boolean enabled) {
	}

	@Override
	public List<Tile> pathToTarget(MobileLifeform pacMan) {
		return target != null ? graph.shortestPath(pacMan.tileLocation(), target) : Collections.emptyList();
	}

	private boolean avoidTouchingGhostAhead() {
		// is dangerous ghost just in front of pacMan and is moving in the same direction?
		Ghost enemy = dangerousGhostsInRange(2).filter(ghost -> me.moveDir() == ghost.moveDir()).findAny().orElse(null);
		if (enemy != null) {
			me.reverseDirection();
			return true;
		}
		return false;
	}

	private boolean avoidOncomingGhost() {
		// is dangerous ghost coming directly towards pacMan?
		Ghost enemy = dangerousGhostsInRange(4).filter(ghost -> me.moveDir() == ghost.moveDir().opposite()).findAny()
				.orElse(null);
		if (enemy != null) {
			me.reverseDirection();
			return true;
		}
		return false;
	}

	private boolean chaseFrightenedGhost(int range) {
		Optional<Ghost> frightenedGhost = ghostsInRange(range).filter(this::isGhostFrightened).findAny();
		if (frightenedGhost.isPresent()) {
			Optional<Direction> dir = frightenedGhost.flatMap(this::directionTowards);
			if (dir.isPresent()) {
				me.setWishDir(dir.get());
				target = frightenedGhost.get().tileLocation();
				return true;
			}
		}
		return false;
	}

	// TODO
	private double distance;

	private void searchFood() {
		distance = Double.MAX_VALUE;
		//@formatter:off
		aheadThenRightThenLeft()
			.filter(me::canCrossBorderTo)
			.forEach(dir -> {
				Tile neighbor = me.neighbor(dir);
				preferredFoodLocationFrom(neighbor).ifPresent(foodLocation -> {
					double d = neighbor.distance(foodLocation);
					if (d < distance) {
						me.setWishDir(dir);
						target = foodLocation;
						distance = d;
					}
				});
			});
		//@formatter:on
	}

	private Stream<Tile> foodTiles() {
		return world.tiles().filter(world::hasFood);
	}

	private Optional<Tile> preferredFoodLocationFrom(Tile here) {
		//@formatter:off
		double nearestEnemyDist = nearestDistanceToDangerousGhost(here);
		if (nearestEnemyDist == Double.MAX_VALUE) {
			return activeBonusAtMostAway(here, 30)
					.or(() -> nearestFoodFrom(here));
		} else {
			return activeBonusAtMostAway(here, 10)
					.or(() -> energizerAtMostAway(here, (int)nearestEnemyDist))
					.or(() -> nearestFoodFrom(here));
		}
		//@formatter:on
	}

	private Optional<Tile> activeBonusAtMostAway(Tile here, int maxDistance) {
		//@formatter:off
		if (world.bonusFood().isPresent()) {
			BonusFood bonus = world.bonusFood().get();
			if (bonus.isPresent()) {
				int dist = here.manhattanDistance(bonus.location());
				if (dist <= maxDistance) {
					return Optional.of(bonus.location());
				}
			}
		}
		return Optional.empty();
	}

	private Optional<Tile> energizerAtMostAway(Tile here, int distance) {
		//@formatter:off
		return foodTiles()
				.filter(tile -> world.hasFood(Pellet.ENERGIZER, tile))
				.filter(energizer -> here.manhattanDistance(energizer) <= distance)
				.findFirst();
		//@formatter:on
	}

	private Optional<Tile> nearestFoodFrom(Tile here) {
		//@formatter:off
		return foodTiles()
				.sorted(comparingInt(food -> here.manhattanDistance(food)))
				.findFirst();
		//@formatter:on
	}

	private boolean isGhostFrightened(Ghost ghost) {
		return ghost.is(FRIGHTENED);
	}

	private boolean isGhostDangerous(Ghost ghost) {
		return ghost.is(CHASING, SCATTERING);
	}

	private boolean isGhostInRange(Ghost ghost, int numTiles) {
		return shortestPathLength(ghost.tileLocation(), me.tileLocation()) <= numTiles;
	}

	private Stream<Ghost> dangerousGhosts() {
		return folks.ghostsInWorld().filter(this::isGhostDangerous);
	}

	private Stream<Ghost> dangerousGhostsInRange(int numTiles) {
		return ghostsInRange(numTiles).filter(this::isGhostDangerous);
	}

	private double nearestDistanceToDangerousGhost(Tile here) {
		return dangerousGhosts().map(ghost -> here.distance(ghost.tileLocation())).min(Double::compareTo)
				.orElse(Double.MAX_VALUE);
	}

	private Stream<Ghost> ghostsInRange(int numTiles) {
		return folks.ghostsInWorld().filter(ghost -> isGhostInRange(ghost, numTiles));
	}

	private Stream<Direction> aheadThenRightThenLeft() {
		return Stream.of(me.moveDir(), me.moveDir().right(), me.moveDir().left());
	}

	private int shortestPathLength(Tile from, Tile to) {
		return graph.shortestPath(from, to).size();
	}

	private Optional<Direction> directionTowards(Ghost enemy) {
		Direction result = null;
		double minDist = Integer.MAX_VALUE;
		for (Direction dir : Direction.values()) {
			if (me.canCrossBorderTo(dir)) {
				List<Tile> path = graph.shortestPath(me.tileLocation(), enemy.tileLocation());
				int dist = path.size();
				if (dist < minDist && path.size() >= 2) {
					minDist = dist;
					result = path.get(0).dirTo(path.get(1)).get();
				}
			}
		}
		return Optional.ofNullable(result);
	}
}