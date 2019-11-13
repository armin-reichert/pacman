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

		// Should actor enter ghost house?
		if (maze.inFrontOfGhostHouseDoor(actorTile) && maze.inGhostHouse(actor.targetTile)) {
			actor.nextDir = Top4.S;
			return;
		}

		// If actor inside ghost house, use path finder for either leaving the ghost house again (target
		// Blinky's home tile) or reaching the target tile inside the ghost house
		// TODO how did the original game do that?
		if (maze.inGhostHouse(actorTile)) {
			List<Tile> path = maze.findPath(actorTile,
					maze.inGhostHouse(actor.targetTile) ? actor.targetTile : maze.blinkyHome);
			actor.targetPath = path;
			actor.nextDir = maze.alongPath(path).orElse(actor.moveDir);
			return;
		}

		// If actor got stuck, check if left or right turn is possible
//		if (actor.isStuck()) {
//			int left = NESW.left(actor.moveDir), right = NESW.right(actor.moveDir);
//			if (actor.canEnterTileTo(left)) {
//				actor.nextDir = left;
//				return;
//			}
//			else if (actor.canEnterTileTo(right)) {
//				actor.nextDir = right;
//				return;
//			}
//		}

		// If newly entered tile is an intersection, decide where to go:
		if (actor.enteredNewTile) {
			// try directions in order: up > left > down > right
			Stream.of(Top4.N, Top4.W, Top4.S, Top4.E)
			/*@formatter:off*/
				 // cannot reverse move direction	
				.filter(dir -> dir != NESW.inv(actor.moveDir))
				 // can move up only at unrestricted intersections
				.filter(dir -> !maze.isIntersection(actorTile) || 
						dir != Top4.N || maze.isUnrestrictedIntersection(actorTile))
				 // check if neighbor tile is accessible
				.map(dir -> maze.tileToDir(actorTile, dir))
				.filter(actor::canEnterTile)
				 // if more than one possibility select tile nearest to target
				.sorted((t1, t2) -> 
						Integer.compare(euclideanDistSq(t1, actor.targetTile), euclideanDistSq(t2, actor.targetTile)))
				 // map tile back to direction
				.map(tile -> maze.direction(actorTile, tile).getAsInt())
				.findFirst().ifPresent(dir -> actor.nextDir = dir);
			/*@formatter:on*/
		}
	}

	private static int euclideanDistSq(Tile t1, Tile t2) {
		int dx = t1.col - t2.col, dy = t1.row - t2.row;
		return dx * dx + dy * dy;
	}
}