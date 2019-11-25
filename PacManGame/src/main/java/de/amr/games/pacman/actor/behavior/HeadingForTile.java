package de.amr.games.pacman.actor.behavior;

import static de.amr.games.pacman.model.Maze.NESW;
import static de.amr.graph.grid.impl.Top4.E;
import static de.amr.graph.grid.impl.Top4.N;
import static de.amr.graph.grid.impl.Top4.S;
import static de.amr.graph.grid.impl.Top4.W;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import de.amr.games.pacman.actor.MazeMover;
import de.amr.games.pacman.model.Tile;

/**
 * Steers an actor towards a (possibly changing) target tile. Each time the
 * {@link #steer(MazeMover)} method is called, the target tile is recomputed.
 * 
 * The detailed behavior is described <a href=
 * "http://gameinternals.com/understanding-pac-man-ghost-behavior">here</a>.
 * 
 * @author Armin Reichert
 */
class HeadingForTile implements Steering {

	/** Directions in the order used to compute the next move direction */
	private static final List<Integer> NWSE = Arrays.asList(N, W, S, E);

	private final Supplier<Tile> fnTargetTile;

	/**
	 * Creates a steering which lets an actor head for the target tile supplied by
	 * the given function.
	 * 
	 * @param fnTargetTile function supplying the target tile whenever the
	 *                     {@link #steer(MazeMover)} method is called
	 */
	public HeadingForTile(Supplier<Tile> fnTargetTile) {
		this.fnTargetTile = fnTargetTile;
	}

	@Override
	public void steer(MazeMover actor) {
		if (!actor.enteredNewTile) {
			return; // TODO move this text outside
		}
		Tile targetTile = fnTargetTile.get();
		if (targetTile != null) {
			actor.targetTile = targetTile;
			actor.nextDir = nextDir(actor, actor.moveDir, actor.currentTile(), targetTile);
			actor.targetPath = actor.computePathToTargetTile ? pathToTargetTile(actor) : Collections.emptyList();
		}
	}

	/**
	 * Computes the next move direction as described <a href=
	 * "http://gameinternals.com/understanding-pac-man-ghost-behavior">here.</a>
	 * 
	 * <p>
	 * Note: We use separate parameters for the actor's move direction, current tile
	 * and target tile instead of the members of the actor itself because the
	 * {@link #computePath(MazeMover, Tile)} method uses this method without
	 * actually placing the actor at each tile of the path.
	 * 
	 * @param actor       an actor (normally a ghost)
	 * @param moveDir     the actor's current move direction
	 * @param currentTile the actor's current tile
	 * @param targetTile  the actor's current target tile
	 */
	private static int nextDir(MazeMover actor, int moveDir, Tile currentTile, Tile targetTile) {
		/*@formatter:off*/
		return NWSE.stream()
			.filter(dir -> dir != NESW.inv(moveDir))
			.filter(dir -> actor.canEnterTile(currentTile, actor.maze.tileToDir(currentTile, dir)))
			.sorted((dir1, dir2) -> {
				Tile neighbor1 = actor.maze.tileToDir(currentTile, dir1);
				Tile neighbor2 = actor.maze.tileToDir(currentTile, dir2);
				int cmpByDistance = Integer.compare(distance(neighbor1, targetTile), distance(neighbor2, targetTile));
				return cmpByDistance != 0
					? cmpByDistance
					: Integer.compare(NWSE.indexOf(dir1), NWSE.indexOf(dir2));
			})
			.findFirst().orElseThrow(IllegalStateException::new);
		/*@formatter:on*/
	}

	/**
	 * Computes the complete path the actor would traverse until it would reach the
	 * target tile, a cycle would occur or the borders of the board would be
	 * reached.
	 * 
	 * @param actor actor for which the path is computed
	 * @return the path the actor would take when moving to its target tile
	 */
	private static List<Tile> pathToTargetTile(MazeMover actor) {
		Set<Tile> path = new LinkedHashSet<>();
		Tile currentTile = actor.currentTile();
		int currentDir = actor.moveDir;
		path.add(currentTile);
		while (!currentTile.equals(actor.targetTile)) {
			int nextDir = nextDir(actor, currentDir, currentTile, actor.targetTile);
			Tile nextTile = actor.maze.tileToDir(currentTile, nextDir);
			if (!actor.maze.insideBoard(nextTile) || path.contains(nextTile)) {
				break;
			}
			path.add(nextTile);
			currentTile = nextTile;
			currentDir = nextDir;
		}
		return new ArrayList<>(path);
	}

	/** Straight line distance (squared). */
	private static int distance(Tile t1, Tile t2) {
		int dx = t1.col - t2.col, dy = t1.row - t2.row;
		return dx * dx + dy * dy;
	}
}