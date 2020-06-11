package de.amr.games.pacman.controller.actor.steering.common;

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

import de.amr.games.pacman.controller.actor.MazeMover;
import de.amr.games.pacman.controller.actor.steering.Steering;
import de.amr.games.pacman.model.Direction;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;

/**
 * Steers an actor towards a target tile.
 * 
 * The detailed behavior is described
 * <a href= "http://gameinternals.com/understanding-pac-man-ghost-behavior">here</a>.
 * 
 * @author Armin Reichert
 */
public class HeadingForTargetTile implements Steering {

	/** Directions in the order used to compute the next move direction */
	static final List<Direction> UP_LEFT_DOWN_RIGHT = Arrays.asList(UP, LEFT, DOWN, RIGHT);

	Supplier<Tile> fnTargetTile;
	MazeMover actor;
	List<Tile> targetPath;
	boolean computePath;
	boolean forced;

	public HeadingForTargetTile(MazeMover actor, Supplier<Tile> fnTargetTile) {
		this(actor);
		this.fnTargetTile = fnTargetTile;
	}

	HeadingForTargetTile(MazeMover actor) {
		this.actor = Objects.requireNonNull(actor);
		targetPath = Collections.emptyList();
		computePath = false;
		forced = false;
	}

	@Override
	public void init() {
		targetPath = Collections.emptyList();
	}

	@Override
	public void steer() {
		if (actor.enteredNewTile() || forced) {
			forced = false;
			targetPath = Collections.emptyList();
			Tile targetTile = fnTargetTile.get();
			if (targetTile != null) {
				Direction dirToTarget = dirToTarget(actor.moveDir(), actor.tile(), targetTile);
				actor.setWishDir(dirToTarget);
				actor.setTargetTile(targetTile);
				if (computePath) {
					targetPath = pathTo(targetTile);
				}
			}
		}
	}

	@Override
	public void force() {
		forced = true;
	}

	@Override
	public void enableTargetPathComputation(boolean b) {
		computePath = b;
	}

	@Override
	public boolean requiresGridAlignment() {
		return true;
	}

	@Override
	public List<Tile> targetPath() {
		return new ArrayList<>(targetPath);
	}

	/**
	 * Computes the next move direction as described
	 * <a href= "http://gameinternals.com/understanding-pac-man-ghost-behavior">here.</a>
	 * 
	 * <p>
	 * Note: We use separate parameters for the actor's move direction, current tile and target tile
	 * instead of the members of the actor itself because the {@link #pathTo(Tile)} method uses this
	 * method without actually placing the actor at each tile of the path.
	 */
	private Direction dirToTarget(Direction moveDir, Tile currentTile, Tile targetTile) {
		Function<Direction, Tile> fnNeighbor = dir -> actor.maze().neighbor(currentTile, dir);
		Function<Direction, Double> fnNeighborDistToTarget = dir -> fnNeighbor.apply(dir).distance(targetTile);
		/*@formatter:off*/
		return UP_LEFT_DOWN_RIGHT.stream()
			.filter(dir -> dir != moveDir.opposite())
			.filter(dir -> actor.canMoveBetween(currentTile, fnNeighbor.apply(dir)))
			.sorted(comparing(fnNeighborDistToTarget).thenComparingInt(UP_LEFT_DOWN_RIGHT::indexOf))
			.findFirst()
			// when a ghost is on a portal tile and the steering has just changed it may happen
			// that no direction can be computed. In that case keep the move direction:
			.orElse(actor.moveDir());
		/*@formatter:on*/
	}

	/**
	 * Computes the complete path the actor would traverse until it would reach the given target tile, a
	 * cycle would occur or the path would leave the board.
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
			Tile nextTile = maze.neighbor(currentTile, dir);
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