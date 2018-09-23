package de.amr.games.pacman.navigation;

import static de.amr.games.pacman.model.Maze.NESW;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Supplier;
import java.util.stream.Stream;

import de.amr.easy.grid.impl.Top4;
import de.amr.games.pacman.actor.Actor;
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
public class FollowTargetTile<T extends Actor> implements ActorNavigation<T> {

	private final Supplier<Tile> targetTileSupplier;

	public FollowTargetTile(Supplier<Tile> targetTileSupplier) {
		this.targetTileSupplier = targetTileSupplier;
	}

	@Override
	public MazeRoute getRoute(T mover) {
		final MazeRoute route = new MazeRoute();
		final Maze maze = mover.getMaze();
		final int moverDir = mover.getCurrentDir();
		final Tile moverTile = mover.getTile();

		// keep direction when in tunnel or teleport space
		if (mover.inTeleportSpace() || mover.inTunnel()) {
			route.setDir(moverDir);
			return route;
		}

		// ask for current target tile
		Tile targetTile = targetTileSupplier.get();
		Objects.requireNonNull(targetTile, "Target tile must not be NULL");

		// if target tile is located in teleport space, use suitable tunnel entry
		if (maze.inTeleportSpace(targetTile)) {
			targetTile = targetTile.col < 0 ? maze.getLeftTunnelEntry() : maze.getRightTunnelEntry();
		}
		route.setTargetTile(targetTile);

		// leave ghost house by following route to Blinky's home tile
		if (mover.inGhostHouse()) {
			Optional<Integer> choice = findBestDir(mover, moverTile, maze.getBlinkyHome(),
					Stream.of(moverDir, NESW.left(moverDir), NESW.right(moverDir)));
			if (choice.isPresent()) {
				route.setDir(choice.get());
			}
			return route;
		}

		// if stuck, check if turning left or right is possible
		if (mover.isStuck()) {
			for (int turn : Arrays.asList(NESW.left(moverDir), NESW.right(moverDir))) {
				if (mover.canEnterTile(maze.neighborTile(moverTile, turn).get())) {
					route.setDir(turn);
					return route;
				}
			}
		}

		// decide where to go at ghosthouse door
		if (maze.isGhostHouseEntry(moverTile)) {
			Stream<Integer> choices = Stream.of(Top4.W, Top4.S, Top4.E);
			Optional<Integer> choice = findBestDir(mover, moverTile, targetTile, choices);
			if (choice.isPresent()) {
				route.setDir(choice.get());
				return route;
			}
		}

		// decide where to go if the next tile is an intersection
		Tile nextTile = moverTile.tileTowards(moverDir);
		boolean free = maze.isFreeIntersection(nextTile);
		boolean notUp = maze.isNotUpIntersection(nextTile);
		if (free || notUp) {
			Stream<Integer> choices = Stream.of(moverDir, NESW.left(moverDir), NESW.right(moverDir))
					.filter(dir -> free || dir != Top4.N);
			Optional<Integer> choice = findBestDir(mover, nextTile, targetTile, choices);
			if (choice.isPresent()) {
				route.setDir(choice.get());
				return route;
			}
		}

		// no direction could be determined
		route.setDir(-1);
		return route;
	}

	private Optional<Integer> findBestDir(Actor mover, Tile from, Tile to, Stream<Integer> choices) {
		final Maze maze = mover.getMaze();
		/*@formatter:off*/
		return choices
			.map(dir -> maze.neighborTile(from, dir))
			.filter(Optional::isPresent).map(Optional::get)
			.filter(mover::canEnterTile)
			.sorted((t1, t2) -> Integer.compare(maze.euclidean2(t1, to), maze.euclidean2(t2, to)))
			.map(tile -> maze.direction(from, tile))
			.map(OptionalInt::getAsInt)
			.findFirst();
		/*@formatter:on*/
	}
}