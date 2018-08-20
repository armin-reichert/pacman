package de.amr.games.pacman.navigation.impl;

import static de.amr.games.pacman.model.Maze.NESW;

import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Supplier;

import de.amr.games.pacman.actor.core.MazeMover;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.navigation.MazeRoute;
import de.amr.games.pacman.navigation.Navigation;

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
 */
public class FollowTargetTile implements Navigation {

	private final Maze maze;
	private final Supplier<Tile> targetTileSupplier;

	public FollowTargetTile(Maze maze, Supplier<Tile> targetTileSupplier) {
		this.maze = maze;
		this.targetTileSupplier = targetTileSupplier;
	}

	@Override
	public MazeRoute computeRoute(MazeMover follower) {

		Tile targetTile = targetTileSupplier.get();
		Objects.requireNonNull(targetTile, "Target tile may not be NULL");

		if (maze.inTeleportSpace(targetTile)) {
			int col = targetTile.col > maze.numCols() - 1 ? maze.numCols() - 1 : 0;
			targetTile = new Tile(col, maze.getTunnelRow());
		}

		int currentDir = follower.getCurrentDir();
		Tile currentTile = follower.getTile();

		MazeRoute route = new MazeRoute();
		route.targetTile = targetTile;

		if (follower.inTunnel() || follower.inTeleportSpace()) {
			route.dir = currentDir;
			return route;
		}

		if (follower.inGhostHouse()) {
			route.dir = findBestDir(follower, maze.getBlinkyHome(), currentTile, currentDir).get();
			return route;
		}

		if (!follower.canMove(currentDir)) {
			int toLeft = NESW.left(currentDir);
			if (follower.canEnterTile(maze.neighborTile(currentTile, toLeft).get())) {
				route.dir = toLeft;
				return route;
			}
			int toRight = NESW.right(currentDir);
			if (follower.canEnterTile(maze.neighborTile(currentTile, toRight).get())) {
				route.dir = toRight;
				return route;
			}
		}

		Tile nextTile = maze.neighborTile(currentTile, currentDir).get();
		if (maze.isIntersection(nextTile)) {
			route.dir = findBestDir(follower, targetTile, nextTile, currentDir).get();
			return route;
		}

		route.dir = -1;
		return route;
	}

	private Optional<Integer> findBestDir(MazeMover mover, Tile targetTile, Tile from, int currentDir) {
		return NESW.dirs().boxed()
		/*@formatter:off*/
			.filter(dir -> dir != NESW.inv(currentDir))
			.map(dir -> maze.neighborTile(from, dir))
			.filter(Optional::isPresent)
			.map(Optional::get)
			.filter(mover::canEnterTile)
			.sorted((t1, t2) -> Integer.compare(maze.euclidean2(t1, targetTile), maze.euclidean2(t2, targetTile)))
			.map(tile -> maze.direction(from, tile))
			.map(OptionalInt::getAsInt)
			.findFirst()
			;
		/*@formatter:on*/
	}
}