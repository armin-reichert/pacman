package de.amr.games.pacman.actor.behavior;

import static de.amr.games.pacman.model.Maze.NESW;

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
class HeadingFor implements Behavior {

	private final Supplier<Tile> fnTargetTile;

	public HeadingFor(Supplier<Tile> fnTargetTile) {
		this.fnTargetTile = fnTargetTile;
	}

	@Override
	public Route getRoute(MazeMover actor) {
		Maze maze = actor.maze;
		Tile targetTile = Objects.requireNonNull(fnTargetTile.get(), "Target tile must not be NULL");
		Tile actorTile = actor.currentTile();

		Route route = new Route();
		route.setTarget(targetTile); // only needed for route visualization, not for movement

		// Should actor enter ghost house?
		if (maze.inFrontOfGhostHouseDoor(actorTile) && maze.inGhostHouse(targetTile)) {
			route.setDir(Top4.S);
			return route;
		}

		// If actor is inside ghost house, use path finder for either leaving the ghost house again (target
		// Blinky's home tile) or reaching the target inside the ghost house
		if (maze.inGhostHouse(actorTile)) {
			route.setPath(maze.findPath(actorTile, maze.inGhostHouse(targetTile) ? targetTile : maze.blinkyHome));
			route.setDir(maze.alongPath(route.getPath()).orElse(actor.moveDir));
			return route;
		}

		// If actor got stuck, check if left or right turn is possible
		if (actor.isStuck()) {
			int left = NESW.left(actor.moveDir), right = NESW.right(actor.moveDir);
			if (actor.canEnterTileTo(left)) {
				route.setDir(left);
				return route;
			}
			else if (actor.canEnterTileTo(right)) {
				route.setDir(right);
				return route;
			}
		}

		// If newly entered tile is an intersection, decide where to go:
		if (actor.enteredNewTile && maze.isIntersection(actorTile)) {
			// try directions in order: up > left > down > right
			int nextDir = Stream.of(Top4.N, Top4.W, Top4.S, Top4.E)
			/*@formatter:off*/
				 // cannot reverse move direction	
				.filter(dir -> dir != NESW.inv(actor.moveDir))
				 // cannot only move up at unrestricted intersections
				.filter(dir -> dir != Top4.N || maze.isUnrestrictedIntersection(actorTile))
				 // check if neighbor tile is accessible
				.map(dir -> maze.tileToDir(actorTile, dir))
				.filter(actor::canEnterTile)
				 // if more than one possibility select tile nearest to target
				.sorted((t1, t2) -> Integer.compare(euclideanDist(t1, targetTile), euclideanDist(t2, targetTile)))
				 // map tile back to direction
				.map(tile -> maze.direction(actorTile, tile).getAsInt())
				.findFirst().orElse(-1);
			/*@formatter:on*/
			route.setDir(nextDir);
		}

		return route;
	}

	private static int euclideanDist(Tile t1, Tile t2) {
		int dx = t1.col - t2.col, dy = t1.row - t2.row;
		return dx * dx + dy * dy;
	}
}