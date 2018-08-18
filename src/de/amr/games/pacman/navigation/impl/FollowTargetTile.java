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
 */
public class FollowTargetTile implements Navigation {

	private Supplier<Tile> targetTileSupplier;

	public FollowTargetTile(Supplier<Tile> targetTileSupplier) {
		this.targetTileSupplier = targetTileSupplier;
	}

	private Tile getTargetTile(Maze maze) {
		Tile targetTile = targetTileSupplier.get();
		if (maze.isTeleportSpace(targetTile)) {
			targetTile = new Tile(0, maze.getTunnelRow());
		}
		return targetTile;
	}

	@Override
	public MazeRoute computeRoute(MazeMover follower) {
		Maze maze = follower.getMaze();
		int currentDir = follower.getCurrentDir();
		Tile currentTile = follower.getTile();
		Tile targetTile = getTargetTile(maze);

		MazeRoute route = new MazeRoute();
		route.dir = currentDir; // default: keep current move direction
		route.targetTile = targetTile;

		// Special cases: 
		
		// Leaving the ghost house. Not sure what original game does in that case.
		if (maze.inGhostHouse(currentTile) || maze.isDoor(currentTile)) {
			route.path = maze.findPath(currentTile, targetTile);
			route.dir = maze.alongPath(route.path).orElse(currentDir);
			return route;
		}

		// Keep move direction
		if (follower.inTunnel() || follower.inTeleportSpace()) {
			return route;
		}

		// Find neighbor tile with least Euclidean distance to target tile
//		LOGGER.info(String.format("Current tile: %s, dir:%d", currentTile, currentDir));
		/*@formatter:off*/
		NESW.dirs()
			.filter(dir -> dir != NESW.inv(currentDir))
			.mapToObj(dir -> maze.neighborTile(currentTile, dir))
			.filter(Optional::isPresent)
			.map(Optional::get)
			.filter(tile -> !maze.isDoor(tile))
			.filter(tile -> !maze.isWall(tile))
			.sorted((t1, t2) -> Integer.compare(maze.euclidean2(t1, targetTile), maze.euclidean2(t2, targetTile)))
			.findFirst()
			.ifPresent(tile -> {
				int dir = maze.direction(currentTile, tile).getAsInt();
				route.dir = dir;
//				LOGGER.info(String.format("Next tile:    %s, dir:%d", tile, dir));
			});
		/*@formatter:on*/

		return route;
	}
}