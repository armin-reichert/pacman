package de.amr.games.pacman.navigation;

import static de.amr.games.pacman.model.Maze.NESW;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import de.amr.easy.grid.impl.Top4;
import de.amr.games.pacman.actor.MazeEntity;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;

/**
 * Attempt at implementing the original Ghost behavior as described
 * <a href="http://gameinternals.com/post/2072558330/understanding-pac-man-ghost-behavior">here</a>:
 *
 * <p>
 * <cite> The next step is understanding exactly how the ghosts attempt to reach their target tiles.
 * The ghosts’ AI is very simple and short-sighted, which makes the complex behavior of the ghosts
 * even more impressive. Ghosts only ever plan one step into the future as they move about the maze.
 * </cite>
 * </p>
 * 
 * <p>
 * <cite> Whenever a ghost enters a new tile, it looks ahead to the next tile that it will reach,
 * and makes a decision about which direction it will turn when it gets there. These decisions have
 * one very important restriction, which is that ghosts may never choose to reverse their direction
 * of travel. That is, a ghost cannot enter a tile from the left side and then decide to reverse
 * direction and move back to the left. The implication of this restriction is that whenever a ghost
 * enters a tile with only two exits, it will always continue in the same direction. </cite>
 * </p>
 * 
 * @author Armin Reichert
 */
class FollowTargetTile<T extends MazeEntity> implements Behavior<T> {

	private final Supplier<Tile> targetTileSupplier;

	public FollowTargetTile(Supplier<Tile> targetTileSupplier) {
		this.targetTileSupplier = targetTileSupplier;
	}

	@Override
	public Route getRoute(T actor) {
		final Maze maze = actor.getMaze();
		final int actorDir = actor.getMoveDir();
		final Tile actorTile = actor.getTile();

		// ask for current target tile
		Tile targetTile = targetTileSupplier.get();
		Objects.requireNonNull(targetTile, "Target tile must not be NULL");

		final Route route = new Route();
		route.setTarget(targetTile);

		// use graph path-finder for entering ghost house
		if (maze.isGhostHouseEntry(actorTile) && maze.inGhostHouse(targetTile)) {
			route.setPath(maze.findPath(actorTile, targetTile));
			route.setDir(maze.alongPath(route.getPath()).orElse(actorDir));
			return route;
		}

		// also use path-finder inside ghost house and for exiting ghost house
		if (maze.inGhostHouse(actorTile)) {
			if (maze.inGhostHouse(targetTile)) {
				// follow target inside ghost house
				route.setPath(maze.findPath(actorTile, targetTile));
			} else {
				// first go to Blinky's home tile to exit ghost house
				route.setPath(maze.findPath(actorTile, maze.getBlinkyHome()));
			}
			route.setDir(maze.alongPath(route.getPath()).orElse(actorDir));
			return route;
		}

		// if stuck, check if turning left or right is possible
		if (actor.isStuck()) {
			for (int turnDir : Arrays.asList(NESW.left(actorDir), NESW.right(actorDir))) {
				Tile turnTile = maze.neighborTile(actorTile, turnDir).get();
				if (actor.canEnterTile(turnTile)) {
					route.setDir(turnDir);
					return route;
				}
			}
		}

		// decide where to go if the next tile is an intersection
		final Tile nextTile = actorTile.tileTowards(actorDir);
		final boolean unrestricted = maze.isUnrestrictedIntersection(nextTile);
		final boolean upwardsBlocked = maze.isUpwardsBlockedIntersection(nextTile);
		if (unrestricted || upwardsBlocked) {
			/*@formatter:off*/
			Stream<Integer> choices = NESW.dirs().boxed()
					.filter(dir -> dir != NESW.inv(actorDir))
					.filter(dir -> unrestricted || dir != Top4.N);
			/*@formatter:on*/
			Optional<Integer> choice = findBestDir(actor, nextTile, targetTile, choices);
			if (choice.isPresent()) {
				route.setDir(choice.get());
				return route;
			}
		}

		// no direction could be determined
		route.setDir(-1);
		return route;
	}

	/**
	 * Find direction to neighbor tile with smallest Euclidean distance to target.
	 */
	private static Optional<Integer> findBestDir(MazeEntity actor, Tile nextTile, Tile targetTile,
			Stream<Integer> choices) {
		/*@formatter:off*/
		return choices
			.map(dir -> actor.getMaze().neighborTile(nextTile, dir))
			.filter(Optional::isPresent).map(Optional::get)
			.filter(actor::canEnterTile)
			.sorted(byEuclideanDist(targetTile))
			.map(tile -> actor.getMaze().direction(nextTile, tile).getAsInt())
			.findFirst();
		/*@formatter:on*/
	}

	private static Comparator<Tile> byEuclideanDist(Tile target) {
		return (t1, t2) -> Float.compare(euclideanDist(t1, target), euclideanDist(t2, target));
	}

	private static float euclideanDist(Tile t1, Tile t2) {
		return (t1.col - t2.col) * (t1.col - t2.col) + (t1.row - t2.row) * (t1.row - t2.row);
	}
}