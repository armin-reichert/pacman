package de.amr.games.pacman.actor.behavior.common;

import static de.amr.games.pacman.model.Maze.NESW;
import static de.amr.games.pacman.model.Tile.distanceSq;
import static de.amr.graph.grid.impl.Grid4Topology.E;
import static de.amr.graph.grid.impl.Grid4Topology.N;
import static de.amr.graph.grid.impl.Grid4Topology.S;
import static de.amr.graph.grid.impl.Grid4Topology.W;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BooleanSupplier;

import de.amr.games.pacman.actor.behavior.Steering;
import de.amr.games.pacman.actor.core.MazeMover;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;

/**
 * Steers an actor towards its current target tile.
 * 
 * The detailed behavior is described <a href=
 * "http://gameinternals.com/understanding-pac-man-ghost-behavior">here</a>.
 * 
 * @param <T> type of actor
 * 
 * @author Armin Reichert
 */
public class HeadingForTargetTile<T extends MazeMover> implements Steering<T> {

	/** Directions in the order used to compute the next move direction */
	private static final List<Byte> NWSE = Arrays.asList(N, W, S, E);

	/** Tells if the complete path the actor will take is computed. */
	public BooleanSupplier fnComputePath = () -> false;

	@Override
	public void steer(T actor) {
		if (actor.targetTile() != null && actor.enteredNewTile()) {
			actor.setNextDir(nextDir(actor, actor.moveDir(), actor.tile(), actor.targetTile()));
			actor.setTargetPath(fnComputePath.getAsBoolean() ? pathToTargetTile(actor) : Collections.emptyList());
		}
	}

	/**
	 * Computes the next move direction as described <a href=
	 * "http://gameinternals.com/understanding-pac-man-ghost-behavior">here.</a>
	 * 
	 * <p>
	 * Note: We use separate parameters for the actor's move direction, current tile
	 * and target tile instead of the members of the actor itself because the
	 * {@link #pathToTargetTile(MazeMover)} method uses this method without actually
	 * placing the actor at each tile of the path.
	 */
	private byte nextDir(T actor, int moveDir, Tile currentTile, Tile targetTile) {
		Maze maze = actor.maze();
		/*@formatter:off*/
		return NWSE.stream()
			.filter(dir -> dir != NESW.inv(moveDir))
			.filter(dir -> actor.canMoveBetween(currentTile, maze.tileToDir(currentTile, dir)))
			.sorted((dir1, dir2) -> {
				Tile neighbor1 = maze.tileToDir(currentTile, dir1);
				Tile neighbor2 = maze.tileToDir(currentTile, dir2);
				int cmpByDistance = Integer.compare(distanceSq(neighbor1, targetTile), distanceSq(neighbor2, targetTile));
				return cmpByDistance != 0
					? cmpByDistance
					: Integer.compare(NWSE.indexOf(dir1), NWSE.indexOf(dir2));
			})
			.findFirst().orElseThrow(IllegalStateException::new);
		/*@formatter:on*/
	}

	/**
	 * Computes the complete path the actor would traverse until it would reach the
	 * target tile, a cycle would occur or the path would leave the board.
	 * 
	 * @param actor actor for which the path is computed
	 * @return the path the actor would take when moving to its target tile
	 */
	private List<Tile> pathToTargetTile(T actor) {
		Maze maze = actor.maze();
		Set<Tile> path = new LinkedHashSet<>();
		Tile currentTile = actor.tile();
		int currentDir = actor.moveDir();
		path.add(currentTile);
		while (!currentTile.equals(actor.targetTile())) {
			byte nextDir = nextDir(actor, currentDir, currentTile, actor.targetTile());
			Tile nextTile = maze.tileToDir(currentTile, nextDir);
			if (!maze.insideBoard(nextTile) || path.contains(nextTile)) {
				break;
			}
			path.add(nextTile);
			currentTile = nextTile;
			currentDir = nextDir;
		}
		return new ArrayList<>(path);
	}
}