package de.amr.games.pacman.controller.steering.pacman;

import static de.amr.games.pacman.controller.creatures.ghost.GhostState.CHASING;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.SCATTERING;
import static java.util.Comparator.comparingInt;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import de.amr.games.pacman.controller.api.MobileCreature;
import de.amr.games.pacman.controller.creatures.ghost.Ghost;
import de.amr.games.pacman.controller.steering.api.PathProvidingSteering;
import de.amr.games.pacman.controller.world.arcade.ArcadeWorldFolks;
import de.amr.games.pacman.model.world.api.Direction;
import de.amr.games.pacman.model.world.core.BonusState;
import de.amr.games.pacman.model.world.core.Tile;
import de.amr.games.pacman.model.world.core.WorldGraph;
import de.amr.games.pacman.model.world.core.WorldGraph.PathFinder;

/**
 * Steering used by PacMan in demo mode.
 * 
 * @author Armin Reichert
 */
public class SearchingForFoodAndAvoidingGhosts implements PathProvidingSteering {

	private final MobileCreature me;
	private final ArcadeWorldFolks folks;
	private WorldGraph graph;
	private Ghost enemy;
	private double distance;
	private Direction newDir;
	private Tile target;

	public SearchingForFoodAndAvoidingGhosts(MobileCreature me, ArcadeWorldFolks folks) {
		this.me = me;
		this.folks = folks;
		graph = new WorldGraph(me.world());
		graph.setPathFinder(PathFinder.ASTAR);
	}

	@Override
	public boolean requiresGridAlignment() {
		return true;
	}

	@Override
	public boolean isPathComputed() {
		return target != null;
	}

	@Override
	public void setPathComputed(boolean enabled) {
	}

	@Override
	public void steer() {
		if (!me.enteredNewTile() && me.canCrossBorderTo(me.moveDir()) || me.world().anyPortalContains(me.location())) {
			return;
		}

		// is dangerous ghost just in front of me moving in the same direction?
		enemy = dangerousGhostInRange(1).filter(ghost -> me.moveDir() == ghost.moveDir()).orElse(null);
		if (enemy != null) {
			me.reverseDirection();
			return;
		}

		// is dangerous ghost coming directly towards me?
		enemy = dangerousGhostInRange(3).filter(ghost -> me.moveDir() == ghost.moveDir().opposite()).orElse(null);
		if (enemy != null) {
			me.reverseDirection();
			return;
		}

		// determine direction for finding food
		target = null;
		distance = Integer.MAX_VALUE;
		//@formatter:off
		aheadThenRightThenLeft()
			.filter(me::canCrossBorderTo)
			.forEach(dir -> {
				Tile neighbor = me.world().neighbor(me.location(), dir);
				preferredFoodLocationFrom(neighbor).ifPresent(foodLocation -> {
					double d = neighbor.distance(foodLocation);
					if (d < distance) {
						newDir = dir;
						distance = d;
						target = foodLocation;
					}
				});
			});
		//@formatter:on
		if (newDir != me.moveDir().opposite()) {
			me.setWishDir(newDir);
			return;
		}
	}

	@Override
	public List<Tile> pathToTarget() {
		return (target != null) ? graph.shortestPath(me.location(), target) : Collections.emptyList();
	}

	private Stream<Ghost> dangerousGhosts() {
		return ghostsInWorld().filter(ghost -> ghost.is(CHASING, SCATTERING));
	}

	private Stream<Ghost> ghostsInWorld() {
		return folks.ghosts().filter(me.world()::contains);

	}

	private Optional<Ghost> dangerousGhostInRange(int dist) {
		//@formatter:off
		return dangerousGhosts()
			.filter(ghost -> shortestPathLength(ghost.location(), me.location()) <= dist)
			.findAny();
		//@formatter:on
	}

	private Stream<Direction> aheadThenRightThenLeft() {
		return Stream.of(me.moveDir(), me.moveDir().right(), me.moveDir().left());
	}

//	private int distance(Stream<Ghost> ghosts) {
//		return ghosts.mapToInt(ghost -> distance(me.tile(), ghost.tile())).min().orElse(Integer.MAX_VALUE);
//	}

	private int shortestPathLength(Tile from, Tile to) {
		return graph.shortestPath(from, to).size();
	}

	private Stream<Tile> foodTiles() {
		return me.world().habitatTiles().filter(me.world()::containsFood);
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

	private double nearestDistanceToDangerousGhost(Tile here) {
		return dangerousGhosts().map(ghost -> here.distance(ghost.location())).min(Double::compareTo)
				.orElse(Double.MAX_VALUE);
	}

	private Optional<Tile> activeBonusAtMostAway(Tile here, int maxDistance) {
		//@formatter:off
		return me.world().getBonus()
				.filter(bonus -> bonus.state == BonusState.ACTIVE)
				.filter(bonus -> here.manhattanDistance(me.world().bonusTile()) <= maxDistance)
				.map(bonus -> me.world().bonusTile());
		//@formatter:on
	}

	private Optional<Tile> energizerAtMostAway(Tile here, int distance) {
		//@formatter:off
		return foodTiles()
				.filter(me.world()::containsEnergizer)
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

}