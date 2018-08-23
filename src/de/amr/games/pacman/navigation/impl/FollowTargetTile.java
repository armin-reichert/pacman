package de.amr.games.pacman.navigation.impl;

import static de.amr.games.pacman.model.Maze.NESW;

import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Supplier;
import java.util.stream.Stream;

import de.amr.easy.grid.impl.Top4;
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

	private final Supplier<Tile> targetTileSupplier;

	public FollowTargetTile(Supplier<Tile> targetTileSupplier) {
		this.targetTileSupplier = targetTileSupplier;
	}

	@Override
	public MazeRoute computeRoute(MazeMover mover) {

		Maze maze = mover.getMaze();
		
		// ask for next target tile
		Tile targetTile = targetTileSupplier.get();
		Objects.requireNonNull(targetTile, "Target tile must not be NULL");

		// if target tile lies in teleport space, take tunnel entry at left or right
		if (maze.inTeleportSpace(targetTile)) {
			int tunnelEntryCol = targetTile.col > (maze.numCols() - 1) ? (maze.numCols() - 1) : 0;
			targetTile = new Tile(tunnelEntryCol, maze.getTunnelRow());
		}

		// create route object for result
		MazeRoute route = new MazeRoute();
		route.targetTile = targetTile;

		int currentDir = mover.getCurrentDir();
		Tile currentTile = mover.getTile();

		// keep direction when in tunnel or teleport space
		if (mover.inTunnel() || mover.inTeleportSpace()) {
			route.dir = currentDir;
			return route;
		}

		// leave ghost house by going to Blinky's home tile
		if (mover.inGhostHouse()) {
			Optional<Integer> choice = findBestDir(mover, maze.getBlinkyHome(), currentTile,
					Stream.of(currentDir, NESW.left(currentDir), NESW.right(currentDir)));
			if (choice.isPresent()) {
				route.dir = choice.get();
			}
			return route;
		}

		// decide to turn left or right if stuck
		if (mover.isStuck()) {
			int toLeft = NESW.left(currentDir);
			if (mover.canEnterTile(maze.neighborTile(currentTile, toLeft).get())) {
				route.dir = toLeft;
				return route;
			}
			int toRight = NESW.right(currentDir);
			if (mover.canEnterTile(maze.neighborTile(currentTile, toRight).get())) {
				route.dir = toRight;
				return route;
			}
		}

		// decide where to go if the next tile is an intersection
		Tile nextTile = maze.neighborTile(currentTile, currentDir).get();
		if (maze.isIntersection(nextTile)) {
			Optional<Integer> choice = findBestDir(mover, targetTile, nextTile,
					Stream.of(currentDir, NESW.left(currentDir), NESW.right(currentDir)));
			if (choice.isPresent()) {
				route.dir = choice.get();
			}
			return route;
		}

		// same for restricted intersections except that going upwards (Top4.N) is forbidden
		if (maze.isRestrictedIntersection(nextTile)) {
			Optional<Integer> choice = findBestDir(mover, targetTile, nextTile,
					Stream.of(currentDir, NESW.left(currentDir), NESW.right(currentDir))
							.filter(dir -> dir != Top4.N));
			if (choice.isPresent()) {
				route.dir = choice.get();
			}
			return route;
		}

		// no direction could be determined
		route.dir = -1;
		return route;
	}

	private Optional<Integer> findBestDir(MazeMover mover, Tile targetTile, Tile fromTile,
			Stream<Integer> dirChoices) {
		Maze maze = mover.getMaze();
		/*@formatter:off*/
		return dirChoices
			.map(dir -> maze.neighborTile(fromTile, dir))
			.filter(Optional::isPresent).map(Optional::get)
			.filter(mover::canEnterTile)
			.sorted((t1, t2) -> Integer.compare(maze.euclidean2(t1, targetTile), maze.euclidean2(t2, targetTile)))
			.map(tile -> maze.direction(fromTile, tile))
			.map(OptionalInt::getAsInt)
			.findFirst();
		/*@formatter:on*/
	}
}