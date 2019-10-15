package de.amr.games.pacman.actor.behavior;

import static de.amr.games.pacman.model.Maze.NESW;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import de.amr.games.pacman.actor.MazeMover;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;
import de.amr.graph.grid.impl.Top4;

/**
 * Attempt at implementing the original Ghost behavior as described
 * <a href="http://gameinternals.com/post/2072558330/understanding-pac-man-ghost-behavior">here</a>:
 *
 * <p>
 * The next step is understanding exactly how the ghosts attempt to reach their target tiles. The
 * ghosts’ AI is very simple and short-sighted, which makes the complex behavior of the ghosts even
 * more impressive. Ghosts only ever plan one step into the future as they move about the maze.
 * <br/>
 * Whenever a ghost enters a new tile, it looks ahead to the next tile that it will reach, and makes
 * a decision about which direction it will turn when it gets there. These decisions have one very
 * important restriction, which is that ghosts may never choose to reverse their direction of
 * travel. That is, a ghost cannot enter a tile from the left side and then decide to reverse
 * direction and move back to the left. The implication of this restriction is that whenever a ghost
 * enters a tile with only two exits, it will always continue in the same direction. </cite>
 * </p>
 * <p>
 * TODO: how did the original game implement exiting and leaving the ghost house?
 * </p>
 * 
 * @author Armin Reichert
 */
class FollowTargetTile<T extends MazeMover> implements Behavior<T> {

	private final Supplier<Tile> targetTileSupplier;

	public FollowTargetTile(Supplier<Tile> targetTileSupplier) {
		this.targetTileSupplier = targetTileSupplier;
	}

	@Override
	public Route getRoute(T actor) {
		final Maze maze = actor.game.maze;
		final int actorDir = actor.getMoveDir();
		final Tile actorTile = actor.getTile();
		final Tile targetTile = Objects.requireNonNull(targetTileSupplier.get(),
				"Target tile must not be NULL");
		final Route route = new Route();
		route.setTarget(targetTile);

		// entering ghost house from above?
		if (maze.isGhostHouseEntry(actorTile) && maze.inGhostHouse(targetTile)) {
			route.setDir(Top4.S);
			return route;
		}

		// if inside ghost house, use path finder. To leave the ghost house, target Blinky's home tile
		if (maze.inGhostHouse(actorTile)) {
			route.setPath(maze.findPath(actorTile,
					maze.inGhostHouse(targetTile) ? targetTile : maze.getBlinkyHome()));
			route.setDir(maze.alongPath(route.getPath()).orElse(actorDir));
			return route;
		}

		// if stuck, check if turning left or right is possible
		if (actor.isStuck()) {
			int left = NESW.left(actorDir), right = NESW.right(actorDir);
			if (actor.canEnterTile(maze.neighborTile(actorTile, left).get())) {
				route.setDir(left);
			}
			else if (actor.canEnterTile(maze.neighborTile(actorTile, right).get())) {
				route.setDir(right);
			}
			return route;
		}

		// If next tile is an intersection, decide where to go:
		final Tile nextTile = actorTile.tileTowards(actorDir);
		final boolean unrestricted = maze.isUnrestrictedIntersection(nextTile);
		final boolean upwardsBlocked = maze.isUpwardsBlockedIntersection(nextTile);
		if (unrestricted || upwardsBlocked) {
			// direction order: up > left > down > right
			int bestDir = Stream.of(Top4.N, Top4.W, Top4.S, Top4.E)
			/*@formatter:off*/
				.filter(dir -> dir != NESW.inv(actorDir)) // cannot reverse direction
				.filter(dir -> dir != Top4.N || unrestricted) // cannot go up if restricted
				.map(dir -> maze.neighborTile(nextTile, dir)) // map direction to neighbor tile
				.filter(Optional::isPresent).map(Optional::get)
				.filter(actor::canEnterTile)
				.sorted((t1, t2) -> Integer.compare(distance(t1, targetTile),	distance(t2, targetTile)))
				.map(tile -> maze.direction(nextTile, tile).getAsInt()) // map tile back to direction
				.findFirst() // sort is stable, thus direction order is preserved
				.orElse(-1);
			/*@formatter:on*/
			route.setDir(bestDir);
			return route;
		}

		return route;
	}

	private static int distance(Tile t1, Tile t2) {
		int dx = t1.col - t2.col, dy = t1.row - t2.row;
		return dx * dx + dy * dy;
	}
}