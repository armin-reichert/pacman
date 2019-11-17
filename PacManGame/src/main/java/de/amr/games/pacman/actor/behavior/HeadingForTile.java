package de.amr.games.pacman.actor.behavior;

import static de.amr.games.pacman.model.Maze.NESW;
import static de.amr.graph.grid.impl.Top4.E;
import static de.amr.graph.grid.impl.Top4.N;
import static de.amr.graph.grid.impl.Top4.S;
import static de.amr.graph.grid.impl.Top4.W;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import de.amr.games.pacman.actor.MazeMover;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;
import de.amr.graph.grid.impl.Top4;

/**
 * Attempt at implementing the original Ghost behavior as described <a href=
 * "http://gameinternals.com/post/2072558330/understanding-pac-man-ghost-behavior">here</a>:
 *
 * <p>
 * The next step is understanding exactly how the ghosts attempt to reach their
 * target tiles. The ghosts’ AI is very simple and short-sighted, which makes
 * the complex behavior of the ghosts even more impressive. Ghosts only ever
 * plan one step into the future as they move about the maze. <br/>
 * Whenever a ghost enters a new tile, it looks ahead to the next tile that it
 * will reach, and makes a decision about which direction it will turn when it
 * gets there. These decisions have one very important restriction, which is
 * that ghosts may never choose to reverse their direction of travel. That is, a
 * ghost cannot enter a tile from the left side and then decide to reverse
 * direction and move back to the left. The implication of this restriction is
 * that whenever a ghost enters a tile with only two exits, it will always
 * continue in the same direction. </cite>
 * </p>
 * 
 * @author Armin Reichert
 */
class HeadingForTile implements Steering {

	/*
	 * Straight line distance (squared).
	 */
	private static int dist(Tile t1, Tile t2) {
		int dx = t1.col - t2.col, dy = t1.row - t2.row;
		return dx * dx + dy * dy;
	}

	private static OptionalInt computeNextDir(MazeMover actor, int moveDir, Tile currentTile, Tile targetTile) {
		/*@formatter:off*/
		Maze maze = actor.maze;
		return IntStream.of(N, W, S, E)
			.filter(dir -> dir != NESW.inv(moveDir))
			.mapToObj(dir -> maze.tileToDir(currentTile, dir))
			.filter(tile -> actor.canEnterTile(currentTile, tile))
			.sorted((t1, t2) -> Integer.compare(dist(t1, targetTile), dist(t2, targetTile)))
			.mapToInt(neighborTile -> maze.direction(currentTile, neighborTile).getAsInt())
			.findFirst();
		/*@formatter:on*/
	}

	private static List<Tile> computePathToTarget(MazeMover actor, Tile targetTile) {
		Maze maze = actor.maze;
		Tile currentTile = actor.currentTile();
		int currentDir = actor.moveDir;
		Set<Tile> path = new LinkedHashSet<>();
		path.add(currentTile);
		while (!currentTile.equals(targetTile)) {
			OptionalInt optNextDir = computeNextDir(actor, currentDir, currentTile, targetTile);
			if (!optNextDir.isPresent()) {
				break;
			}
			int nextDir = optNextDir.getAsInt();
			Tile nextTile = maze.tileToDir(currentTile, nextDir);
			if (!maze.insideBoard(nextTile)) {
				break;
			}
			if (path.contains(nextTile)) {
				break; // cycle
			}
			path.add(nextTile);
			currentTile = nextTile;
			currentDir = nextDir;
		}
		return path.stream().collect(Collectors.toList());
	}

	private final Supplier<Tile> fnTargetTile;

	public HeadingForTile(Supplier<Tile> fnTargetTile) {
		this.fnTargetTile = fnTargetTile;
	}

	@Override
	public void steer(MazeMover actor) {
		Tile actorTile = actor.currentTile();
		actor.targetTile = Objects.requireNonNull(fnTargetTile.get(), "Target tile must not be NULL");

		/* Entering ghost house: move downwards at the ghost house door. */
		if (actor.maze.inGhostHouse(actor.targetTile) && actor.maze.inFrontOfGhostHouseDoor(actorTile)) {
			actor.nextDir = Top4.S;
			actor.targetPath = actor.visualizeCompletePath ? computePathToTarget(actor, actor.targetTile)
					: Collections.emptyList();
			return;
		}

		/* For leaving the ghost house use Blinky's home as target tile. */
		if (actor.maze.inGhostHouse(actorTile) && !actor.maze.inGhostHouse(actor.targetTile)) {
			actor.targetTile = actor.maze.blinkyHome;
		}

		/* If a new tile is entered, decide where to go as described above. */
		if (actor.enteredNewTile) {
			computeNextDir(actor, actor.moveDir, actorTile, actor.targetTile).ifPresent(dir -> actor.nextDir = dir);
		}

		actor.targetPath = actor.visualizeCompletePath ? computePathToTarget(actor, actor.targetTile)
				: Collections.emptyList();
	}
}