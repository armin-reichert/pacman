package de.amr.games.pacman.controller.actor.steering.pacman;

import static java.util.Comparator.comparingInt;

import java.util.Optional;
import java.util.stream.Stream;

import de.amr.games.pacman.PacManApp;
import de.amr.games.pacman.controller.actor.Creature;
import de.amr.games.pacman.controller.actor.Ghost;
import de.amr.games.pacman.controller.actor.GhostState;
import de.amr.games.pacman.controller.actor.WorldMover;
import de.amr.games.pacman.controller.actor.steering.Steering;
import de.amr.games.pacman.model.Direction;
import de.amr.games.pacman.model.world.core.BonusState;
import de.amr.games.pacman.model.world.core.Tile;
import de.amr.games.pacman.model.world.core.WorldGraph;
import de.amr.graph.grid.api.GridGraph2D;

/**
 * Steering used by PacMan in demo mode.
 * 
 * @author Armin Reichert
 */
public class SearchingForFoodAndAvoidingGhosts implements Steering {

	private final WorldMover me;
	private WorldGraph graph;
	private Ghost enemy;
	private int distance;
	private Direction newDir;

	public SearchingForFoodAndAvoidingGhosts(Creature<?> me) {
		this.me = me;
		graph = new WorldGraph(me.world());
		PacManApp.settings.pathFinder = "bestfs";
	}

	@Override
	public boolean requiresGridAlignment() {
		return true;
	}

	@Override
	public void steer() {
		if (!me.enteredNewTile() && me.canCrossBorderTo(me.moveDir())) {
			return;
		}

		// is dangerous ghost just in front of me moving in the same direction?
		enemy = dangerousGhostInRange(1).filter(ghost -> me.moveDir() == ghost.moveDir()).orElse(null);
		if (enemy != null) {
			me.reverseDirection();
			return;
		}

		// is dangerous ghost coming directly towards me?
		enemy = dangerousGhostInRange(4).filter(ghost -> me.moveDir() == ghost.moveDir().opposite()).orElse(null);
		if (enemy != null) {
			me.setWishDir(enemy.moveDir());
			me.forceMoving(me.wishDir());
			return;
		}

		// is dangerous ghost nearby?
		enemy = dangerousGhostInRange(10).orElse(null);
		if (enemy != null) {
			distance = -1;
			Direction.dirsShuffled().forEach(dir -> {
				Tile neighbor = me.world().neighbor(me.tile(), dir);
				if (me.world().isAccessible(neighbor)) {
					int d = distance(neighbor, enemy.tile());
					if (d > distance) {
						d = distance;
						newDir = dir;
					}
				}
			});
			if (newDir != me.moveDir().opposite()) {
				me.setWishDir(newDir);
				return;
			}
		}

		// determine direction for finding food
		distance = Integer.MAX_VALUE;
		//@formatter:off
		dirsInOrder()
			.filter(me::canCrossBorderTo)
			.forEach(dir -> {
				Tile neighbor = me.world().neighbor(me.tile(), dir);
				preferredFoodLocationFrom(neighbor).ifPresent(foodLocation -> {
					int d = neighbor.manhattanDistance(foodLocation);
					if (d < distance) {
						newDir = dir;
						distance = d;
					}
				});
			});
		//@formatter:off
		if (newDir != me.moveDir().opposite()) {
			me.setWishDir(newDir);
			return;
		}
	}


	private Stream<Ghost> dangerousGhosts() {
		//@formatter:off
		return me.world().population().ghosts()
				.filter(me.world()::included)
				.filter(ghost -> ghost.getState() == GhostState.CHASING || ghost.getState() == GhostState.SCATTERING);
		//@formatter:on
	}

	private Optional<Ghost> dangerousGhostInRange(int dist) {
		//@formatter:off
		return dangerousGhosts()
			.filter(ghost -> distance(ghost.tile(), me.tile()) <= dist)
			.findAny();
		//@formatter:on
	}
	
	private Stream<Direction> dirsInOrder() {
		return Stream.of(me.moveDir(), me.moveDir().right(), me.moveDir().left());
	}

	private int distance(Stream<Ghost> ghosts) {
		return ghosts.mapToInt(ghost -> distance(me.tile(), ghost.tile())).min().orElse(Integer.MAX_VALUE);
	}

	private int distance(Tile from, Tile to) {
		return graph.shortestPath(from, to).size();
	}

	private Stream<Tile> foodTiles() {
		return me.world().habitatTiles().filter(me.world()::containsFood);
	}

	private Optional<Tile> preferredFoodLocationFrom(Tile here) {
		return activeBonusAtMostAway(here, 40).or(() -> energizerAtMostAway(here, 20)).or(() -> nearestFoodFrom(here));
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