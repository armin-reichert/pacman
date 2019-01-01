package de.amr.games.pacman.navigation;

import static de.amr.games.pacman.model.Maze.NESW;

import java.util.List;
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
		final Route route = new Route();

		// where to go?
		final Tile targetTile = targetTileSupplier.get();
		Objects.requireNonNull(targetTile, "Target tile must not be NULL");
		route.setTarget(targetTile);

		final Maze maze = actor.getMaze();
		final int actorDir = actor.getMoveDir();
		final Tile actorTile = actor.getTile();

		// use graph path-finder for entering ghost house
		if (maze.isGhostHouseEntry(actorTile) && maze.inGhostHouse(targetTile)) {
			List<Tile> intoGhostHouse = maze.findPath(actorTile, targetTile);
			route.setPath(intoGhostHouse);
			route.setDir(maze.alongPath(intoGhostHouse).orElse(actorDir));
			return route;
		}

		// also use path-finder inside ghost house and for exiting ghost house
		if (maze.inGhostHouse(actorTile)) {
			if (maze.inGhostHouse(targetTile)) {
				// follow target inside ghost house
				route.setPath(maze.findPath(actorTile, targetTile));
			} else {
				// go to Blinky's home to exit ghost house
				route.setPath(maze.findPath(actorTile, maze.getBlinkyHome()));
			}
			route.setDir(maze.alongPath(route.getPath()).orElse(actorDir));
			return route;
		}

		// if stuck, check if turning left or right is possible
		if (actor.isStuck()) {
			int left = NESW.left(actorDir);
			if (actor.canEnterTile(maze.neighborTile(actorTile, left).get())) {
				route.setDir(left);
				return route;
			}
			int right = NESW.right(actorDir);
			if (actor.canEnterTile(maze.neighborTile(actorTile, right).get())) {
				route.setDir(right);
				return route;
			}
		}

		// decide where to go if the next tile is an intersection
		final Tile nextTile = actorTile.tileTowards(actorDir);
		final boolean unrestricted = maze.isUnrestrictedIntersection(nextTile);
		final boolean upwardsBlocked = maze.isUpwardsBlockedIntersection(nextTile);
		if (unrestricted || upwardsBlocked) {
			// preference: up > left > down > right
			Stream<Integer> dirs = Stream.of(Top4.N, Top4.W, Top4.S, Top4.E)
					.filter(dir -> dir != NESW.inv(actorDir)).filter(dir -> dir != Top4.N || unrestricted);
			route.setDir(findBestDir(actor, nextTile, targetTile, dirs));
			return route;
		}

		// no direction could be determined
		route.setDir(-1);
		return route;
	}

	/**
	 * Finds the "best" direction from the source tile towards the target tile, i.e. the direction to
	 * the (accessible) neighbor tile with the smallest (straight line) distance from the target tile.
	 */
	private int findBestDir(MazeEntity actor, Tile sourceTile, Tile targetTile, Stream<Integer> choices) {
		/*@formatter:off*/
		return choices
			.map(dir -> actor.getMaze().neighborTile(sourceTile, dir)) // map direction to neighbor tile
			.filter(Optional::isPresent).map(Optional::get)
			.filter(actor::canEnterTile)
			.sorted((t1, t2) -> Integer.compare(straightDistance(t1, targetTile),	straightDistance(t2, targetTile)))
			.map(tile -> actor.getMaze().direction(sourceTile, tile).getAsInt()) // map tile back to direction
			.findFirst().orElse(-1);
		/*@formatter:on*/
	}

	private static int straightDistance(Tile t1, Tile t2) {
		int dx = t1.col - t2.col, dy = t1.row - t2.row;
		return dx * dx + dy * dy;
	}
}