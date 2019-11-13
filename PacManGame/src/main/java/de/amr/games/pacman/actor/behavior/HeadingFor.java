package de.amr.games.pacman.actor.behavior;

import static de.amr.games.pacman.model.Maze.NESW;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Stream;

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
class HeadingFor implements SteeringBehavior {

	private final Supplier<Tile> fnTargetTile;

	public HeadingFor(Supplier<Tile> fnTargetTile) {
		this.fnTargetTile = fnTargetTile;
	}

	@Override
	public void steer(MazeMover actor) {
		Maze maze = actor.maze;
		Tile actorTile = actor.currentTile();

		actor.targetPath = Collections.emptyList();
		actor.targetTile = Objects.requireNonNull(fnTargetTile.get(), "Target tile must not be NULL");

		/*
		 * If the actor wants to enter the ghost house, check if it is at the ghost
		 * house door and if yes, move down.
		 */
		if (maze.inGhostHouse(actor.targetTile) && maze.inFrontOfGhostHouseDoor(actorTile)) {
			actor.nextDir = Top4.S;
			return;
		}

		/*
		 * If the actor is in the ghost house, use the graph path finder for either
		 * leaving the ghost house again (by targetting Blinky's home tile) or for
		 * reaching the target tile inside the ghost house.
		 * 
		 * TODO: how did the original game do that?
		 */
		if (maze.inGhostHouse(actorTile)) {
			List<Tile> path = maze.findPath(actorTile,
					maze.inGhostHouse(actor.targetTile) ? actor.targetTile : maze.blinkyHome);
			actor.targetPath = path;
			actor.nextDir = maze.alongPath(path).orElse(actor.moveDir);
			return;
		}

		/* If a new tile is entered, decide where to go as described above. */
		if (actor.enteredNewTile) {
			// try directions in order: up > left > down > right
			Stream.of(Top4.N, Top4.W, Top4.S, Top4.E)
			/*@formatter:off*/
				 // never reverse direction	
				.filter(dir -> dir != NESW.inv(actor.moveDir))
				 // cannot move upwards at blocked intersection
				.filter(dir -> !(dir == Top4.N && maze.isUpwardsBlockedIntersection(actorTile)))
				 // check if neighbor tile to this direction is accessible
				.map(dir -> maze.tileToDir(actorTile, dir))
				.filter(actor::canEnterTile)
				 // select tile with smallest straight line distance to target
				.sorted((t1, t2) -> Integer.compare(dist(t1, actor.targetTile), dist(t2, actor.targetTile)))
				.findFirst()
				 // map back to direction
				.map(neighborTile -> maze.direction(actorTile, neighborTile).getAsInt())
				// use result as new intended direction
				.ifPresent(dir -> actor.nextDir = dir);
			/*@formatter:on*/
		}
	}

	/*
	 * Straight line distance (squared).
	 */
	private static int dist(Tile t1, Tile t2) {
		int dx = t1.col - t2.col, dy = t1.row - t2.row;
		return dx * dx + dy * dy;
	}
}