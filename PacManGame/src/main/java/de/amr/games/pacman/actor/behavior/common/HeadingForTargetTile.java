package de.amr.games.pacman.actor.behavior.common;

import static de.amr.games.pacman.model.Direction.DOWN;
import static de.amr.games.pacman.model.Direction.LEFT;
import static de.amr.games.pacman.model.Direction.RIGHT;
import static de.amr.games.pacman.model.Direction.UP;
import static java.util.Collections.emptyList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

import de.amr.games.pacman.actor.behavior.Steering;
import de.amr.games.pacman.actor.core.MazeMover;
import de.amr.games.pacman.model.Direction;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;

/**
 * Steers an actor towards a target tile.
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
	private static final List<Direction> NWSE = Arrays.asList(UP, LEFT, DOWN, RIGHT);

	private Supplier<Tile> fnTargetTile;

	public HeadingForTargetTile(Supplier<Tile> fnTargetTile) {
		this.fnTargetTile = Objects.requireNonNull(fnTargetTile);
	}

	@Override
	public void steer(T actor) {
		Tile targetTile = fnTargetTile.get();
		if (targetTile != null && actor.enteredNewTile()) {
			actor.setNextDir(nextDir(actor, actor.moveDir(), actor.tile(), targetTile));
			actor.setTargetTile(targetTile);
			actor.setTargetPath(actor.requireTargetPath() ? pathToTargetTile(actor, targetTile) : emptyList());
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
	private Direction nextDir(T actor, Direction moveDir, Tile currentTile, Tile targetTile) {
		Maze maze = actor.maze();
		/*@formatter:off*/
		return NWSE.stream()
			.filter(dir -> dir != moveDir.opposite())
			.filter(dir -> actor.canMoveBetween(currentTile, maze.tileToDir(currentTile, dir)))
			.sorted((dir1, dir2) -> {
				Tile neighbor1 = maze.tileToDir(currentTile, dir1);
				Tile neighbor2 = maze.tileToDir(currentTile, dir2);
				int cmpByDistance = Integer.compare(Tile.distanceSq(neighbor1, targetTile), Tile.distanceSq(neighbor2, targetTile));
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
	private List<Tile> pathToTargetTile(T actor, Tile targetTile) {
		Maze maze = actor.maze();
		Set<Tile> path = new LinkedHashSet<>();
		Tile currentTile = actor.tile();
		Direction currentDir = actor.moveDir();
		path.add(currentTile);
		while (!currentTile.equals(targetTile)) {
			Direction nextDir = nextDir(actor, currentDir, currentTile, targetTile);
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