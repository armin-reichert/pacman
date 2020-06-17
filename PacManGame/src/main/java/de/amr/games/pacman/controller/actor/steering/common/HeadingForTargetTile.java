package de.amr.games.pacman.controller.actor.steering.common;

import static de.amr.games.pacman.model.Direction.DOWN;
import static de.amr.games.pacman.model.Direction.LEFT;
import static de.amr.games.pacman.model.Direction.RIGHT;
import static de.amr.games.pacman.model.Direction.UP;
import static java.util.Comparator.comparing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

import de.amr.games.pacman.controller.actor.MovingThroughMaze;
import de.amr.games.pacman.controller.actor.steering.PathProvidingSteering;
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
public class HeadingForTargetTile implements PathProvidingSteering {

	/** Directions in the order used to compute the next move direction */
	private static final List<Direction> UP_LEFT_DOWN_RIGHT = Arrays.asList(UP, LEFT, DOWN, RIGHT);

	private final MovingThroughMaze actor;
	private final Supplier<Tile> fnTargetTile;
	private final LinkedHashSet<Tile> path = new LinkedHashSet<>();
	private boolean forced;
	private boolean pathComputationEnabled;

	public HeadingForTargetTile(MovingThroughMaze actor, Supplier<Tile> fnTargetTile) {
		this.actor = Objects.requireNonNull(actor);
		this.fnTargetTile = Objects.requireNonNull(fnTargetTile);
	}

	@Override
	public void steer() {
		if (actor.enteredNewTile() || forced) {
			forced = false;
			Tile targetTile = fnTargetTile.get();
			if (targetTile != null) {
				Direction dirToTarget = dirToTarget(actor.moveDir(), actor.tile(), targetTile);
				actor.setWishDir(dirToTarget);
				actor.setTargetTile(targetTile);
				if (pathComputationEnabled) {
					computePath(targetTile);
				}
			}
		}
	}

	@Override
	public void force() {
		forced = true;
	}

	@Override
	public void setPathComputationEnabled(boolean enabled) {
		if (pathComputationEnabled != enabled) {
			path.clear();
		}
		pathComputationEnabled = enabled;
	}

	@Override
	public boolean isPathComputationEnabled() {
		return pathComputationEnabled;
	}

	@Override
	public boolean requiresGridAlignment() {
		return true;
	}

	@Override
	public List<Tile> pathToTarget() {
		return new ArrayList<>(path);
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
	 * @param targetTile target tile
	 * @return the path the actor would take when moving to its target tile
	 */
	private void computePath(Tile targetTile) {
		Maze maze = actor.maze();
		Tile currentTile = actor.tile();
		Direction currentDir = actor.moveDir();
		path.clear();
		path.add(currentTile);
		while (!currentTile.equals(targetTile)) {
			Direction dir = dirToTarget(currentDir, currentTile, targetTile);
			Tile nextTile = maze.neighbor(currentTile, dir);
			if (!maze.insideMap(nextTile) || path.contains(nextTile)) {
				break;
			}
			path.add(nextTile);
			currentTile = nextTile;
			currentDir = dir;
		}
	}
}