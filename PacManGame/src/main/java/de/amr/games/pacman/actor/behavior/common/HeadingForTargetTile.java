package de.amr.games.pacman.actor.behavior.common;

import static de.amr.games.pacman.model.Direction.DOWN;
import static de.amr.games.pacman.model.Direction.LEFT;
import static de.amr.games.pacman.model.Direction.RIGHT;
import static de.amr.games.pacman.model.Direction.UP;
import static java.util.Comparator.comparing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

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
 * @author Armin Reichert
 */
public class HeadingForTargetTile implements Steering {

	/** Directions in the order used to compute the next move direction */
	private static final List<Direction> UP_LEFT_DOWN_RIGHT = Arrays.asList(UP, LEFT, DOWN, RIGHT);

	private final MazeMover actor;
	private Supplier<Tile> fnTargetTile;
	private List<Tile> targetPath;
	private boolean computePath;

	public HeadingForTargetTile(MazeMover actor, Supplier<Tile> fnTargetTile) {
		this.actor = actor;
		this.fnTargetTile = Objects.requireNonNull(fnTargetTile);
		targetPath = Collections.emptyList();
		computePath = false;
	}

	@Override
	public void computeTargetPath(boolean b) {
		computePath = b;
	}

	@Override
	public boolean stayOnTrack() {
		return true;
	}

	@Override
	public List<Tile> targetPath() {
		return new ArrayList<>(targetPath);
	}

	@Override
	public void steer() {
		if (actor.enteredNewTile()) {
			Tile targetTile = fnTargetTile.get();
			if (targetTile != null) {
				Direction dirToTarget = dirToTarget(actor.moveDir(), actor.tile(), targetTile);
				actor.setWishDir(dirToTarget);
				actor.setTargetTile(targetTile);
				targetPath = computePath ? pathTo(targetTile) : Collections.emptyList();
			} else {
				targetPath = Collections.emptyList();
			}
		}
	}

	/**
	 * Computes the next move direction as described <a href=
	 * "http://gameinternals.com/understanding-pac-man-ghost-behavior">here.</a>
	 * 
	 * <p>
	 * Note: We use separate parameters for the actor's move direction, current tile
	 * and target tile instead of the members of the actor itself because the
	 * {@link #pathTo(Tile)} method uses this method without actually placing the
	 * actor at each tile of the path.
	 */
	private Direction dirToTarget(Direction moveDir, Tile currentTile, Tile targetTile) {
		Function<Direction, Tile> neighbor = dir -> actor.maze().tileToDir(currentTile, dir);
		Function<Direction, Integer> neighborDistToTarget = dir -> Tile.distanceSq(neighbor.apply(dir), targetTile);
		/*@formatter:off*/
		return UP_LEFT_DOWN_RIGHT
			.stream()
			.filter(dir -> dir != moveDir.opposite())
			.filter(dir -> actor.canMoveBetween(currentTile, neighbor.apply(dir)))
			.sorted(comparing(neighborDistToTarget).thenComparingInt(UP_LEFT_DOWN_RIGHT::indexOf))
			.findFirst()
			.orElseThrow(IllegalStateException::new);
		/*@formatter:on*/
	}

	/**
	 * Computes the complete path the actor would traverse until it would reach the
	 * given target tile, a cycle would occur or the path would leave the board.
	 * 
	 * @return the path the actor would take when moving to its target tile
	 */
	private List<Tile> pathTo(Tile targetTile) {
		Maze maze = actor.maze();
		Set<Tile> path = new LinkedHashSet<>();
		Tile currentTile = actor.tile();
		Direction currentDir = actor.moveDir();
		path.add(currentTile);
		while (!currentTile.equals(targetTile)) {
			Direction dir = dirToTarget(currentDir, currentTile, targetTile);
			Tile nextTile = maze.tileToDir(currentTile, dir);
			if (!maze.insideBoard(nextTile) || path.contains(nextTile)) {
				break;
			}
			path.add(nextTile);
			currentTile = nextTile;
			currentDir = dir;
		}
		return new ArrayList<>(path);
	}
}