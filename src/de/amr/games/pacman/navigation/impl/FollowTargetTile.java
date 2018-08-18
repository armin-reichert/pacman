package de.amr.games.pacman.navigation.impl;

import static de.amr.easy.game.Application.LOGGER;
import static de.amr.games.pacman.model.Maze.NESW;

import java.util.Optional;
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
 * 
 * TODO: does not yet work 100% correctly, ghost in some cases still reverses direction
 */
public class FollowTargetTile implements Navigation {

	private Supplier<Tile> targetTileSupplier;

	public FollowTargetTile(Supplier<Tile> targetTileSupplier) {
		this.targetTileSupplier = targetTileSupplier;
	}

	@Override
	public MazeRoute computeRoute(MazeMover follower) {
		Maze maze = follower.getMaze();
		int currentDir = follower.getCurrentDir();
		Tile currentTile = follower.getTile();
		LOGGER.info(String.format("Current tile: %s, dir:%d", currentTile, currentDir));

		Tile targetTile = targetTileSupplier.get();
		if (maze.isTeleportSpace(targetTile)) {
			targetTile = targetTile.col > maze.numCols() - 1 ? new Tile(maze.numCols() - 1, maze.getTunnelRow())
					: new Tile(0, maze.getTunnelRow());
		}

		MazeRoute route = new MazeRoute();
		route.dir = currentDir; // default: keep current move direction
		route.targetTile = targetTile;

		if (follower.inTunnel() || follower.inTeleportSpace()) {
			return route;
		}

		if (follower.inGhostHouse()) {
			selectTileClosestTo(maze.getBlinkyHome(), follower, true).ifPresent(tile -> {
				int dir = maze.direction(currentTile, tile).getAsInt();
				route.dir = dir;
				LOGGER.info(String.format("Next tile:    %s, dir:%d", tile, route.dir));
			});
			return route;
		}

		selectTileClosestTo(targetTile, follower, false).ifPresent(tile -> {
			int dir = maze.direction(currentTile, tile).getAsInt();
			route.dir = dir;
			LOGGER.info(String.format("Next tile:    %s, dir:%d", tile, route.dir));
		});
		return route;
	}

	private Optional<Tile> selectTileClosestTo(Tile targetTile, MazeMover follower, boolean doorOpen) {
		Maze maze = follower.getMaze();
		Tile currentTile = follower.getTile();
		int currentDir = follower.getCurrentDir();
		return NESW.dirs().boxed()
		/*@formatter:off*/
			.filter(dir -> dir != NESW.inv(currentDir))
			.map(dir -> maze.neighborTile(currentTile, dir))
			.filter(Optional::isPresent)
			.map(Optional::get)
			.filter(tile -> !maze.isWall(tile))
			.filter(tile -> !maze.isDoor(tile) || doorOpen)
			.sorted((t1, t2) -> Integer.compare(maze.euclidean2(t1, targetTile), maze.euclidean2(t2, targetTile)))
			.findFirst();
		/*@formatter:on*/
	}
}