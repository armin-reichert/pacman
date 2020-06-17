package de.amr.games.pacman.controller.actor.steering.common;

import static de.amr.games.pacman.model.Direction.DOWN;
import static de.amr.games.pacman.model.Direction.LEFT;
import static de.amr.games.pacman.model.Direction.RIGHT;
import static de.amr.games.pacman.model.Direction.UP;
import static java.util.Arrays.asList;
import static java.util.Comparator.comparing;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

import de.amr.games.pacman.controller.actor.MazeMover;
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

	/**
	 * Computes the next move direction as described
	 * <a href= "http://gameinternals.com/understanding-pac-man-ghost-behavior">here.</a>
	 * <p>
	 * When a ghost is on a portal tile and the steering has just changed, it may happen that no
	 * direction can be computed. In that case we keep the move direction.
	 * <p>
	 * Note: We use separate parameters for the actor's move direction, current tile and target tile
	 * instead of the members of the actor itself because the {@link #pathTo(Tile)} method uses this
	 * method without actually placing the actor at each tile of the path.
	 * 
	 * @param mover      actor moving through the maze
	 * @param moveDir    current move direction
	 * @param tile       current tile
	 * @param targetTile target tile
	 */
	private static Direction bestDir(MazeMover mover, Direction moveDir, Tile tile, Tile targetTile) {
		Function<Direction, Double> fnDistFromNeighborToTarget = dir -> mover.maze().neighbor(tile, dir)
				.distance(targetTile);
		/*@formatter:off*/
		return Direction.dirs()
			.filter(dir -> dir != moveDir.opposite())
			.filter(dir -> mover.canMoveBetween(tile, mover.maze().neighbor(tile, dir)))
			.sorted(comparing(fnDistFromNeighborToTarget).thenComparingInt(asList(UP, LEFT, DOWN, RIGHT)::indexOf))
			.findFirst()
			.orElse(mover.moveDir());
		/*@formatter:on*/
	}

	private final MazeMover actor;
	private final Supplier<Tile> fnTargetTile;
	private final LinkedHashSet<Tile> path = new LinkedHashSet<>();
	private boolean forced;
	private boolean pathComputationEnabled;

	public HeadingForTargetTile(MazeMover actor, Supplier<Tile> fnTargetTile) {
		this.actor = Objects.requireNonNull(actor);
		this.fnTargetTile = Objects.requireNonNull(fnTargetTile);
	}

	@Override
	public void steer() {
		if (actor.enteredNewTile() || forced) {
			forced = false;
			actor.setTargetTile(fnTargetTile.get());
			if (actor.targetTile() != null) {
				actor.setWishDir(bestDir(actor, actor.moveDir(), actor.tile(), actor.targetTile()));
				if (pathComputationEnabled) {
					computePath();
				}
			} else {
				path.clear();
			}
		}
	}

	/**
	 * Computes the path the actor would traverse until reaching the target tile, a cycle would occur or
	 * the path would leave the map.
	 */
	private void computePath() {
		Maze maze = actor.maze();
		Tile currentTile = actor.tile(), targetTile = actor.targetTile();
		Direction currentDir = actor.moveDir();
		path.clear();
		path.add(currentTile);
		while (!currentTile.equals(targetTile)) {
			Direction dir = bestDir(actor, currentDir, currentTile, targetTile);
			Tile nextTile = maze.neighbor(currentTile, dir);
			if (!maze.insideMap(nextTile) || path.contains(nextTile)) {
				return;
			}
			path.add(nextTile);
			currentTile = nextTile;
			currentDir = dir;
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
}