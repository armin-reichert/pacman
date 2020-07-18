package de.amr.games.pacman.controller.steering.common;

import static de.amr.games.pacman.model.world.api.Direction.DOWN;
import static de.amr.games.pacman.model.world.api.Direction.LEFT;
import static de.amr.games.pacman.model.world.api.Direction.RIGHT;
import static de.amr.games.pacman.model.world.api.Direction.UP;
import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Function;
import java.util.function.Supplier;

import de.amr.games.pacman.controller.steering.api.PathProvidingSteering;
import de.amr.games.pacman.controller.steering.api.TargetTileSteering;
import de.amr.games.pacman.model.world.api.Direction;
import de.amr.games.pacman.model.world.api.MobileLifeform;
import de.amr.games.pacman.model.world.api.Tile;

/**
 * Steers an actor towards a target tile.
 * 
 * The detailed behavior is described
 * <a href= "http://gameinternals.com/understanding-pac-man-ghost-behavior">here</a>.
 * 
 * @author Armin Reichert
 */
public class HeadingForTargetTile implements TargetTileSteering, PathProvidingSteering {

	private static final List<Direction> directionPriority = asList(UP, LEFT, DOWN, RIGHT);

	private final MobileLifeform mover;
	private final ConcurrentLinkedDeque<Tile> path = new ConcurrentLinkedDeque<>();
	private Supplier<Tile> fnTargetTile;
	private boolean forced;
	private boolean pathComputed;

	public HeadingForTargetTile(MobileLifeform mover, Supplier<Tile> fnTargetTile) {
		this.mover = Objects.requireNonNull(mover);
		this.fnTargetTile = Objects.requireNonNull(fnTargetTile);
	}

	@Override
	public Tile targetTile() {
		return fnTargetTile.get();
	}

	@Override
	public void setTargetTile(Supplier<Tile> fnTargetTile) {
		this.fnTargetTile = Objects.requireNonNull(fnTargetTile);
	}

	@Override
	public void steer() {
		if (mover.enteredNewTile() || forced) {
			forced = false;
			Tile targetTile = targetTile();
			if (targetTile != null) {
				mover.setWishDir(bestDirTowardsTarget(mover, mover.moveDir(), mover.tileLocation(), targetTile));
				if (pathComputed) {
					computePath(targetTile);
				}
			} else {
				path.clear();
			}
		}
	}

	/**
	 * Computes the next move direction as described
	 * <a href= "http://gameinternals.com/understanding-pac-man-ghost-behavior">here.</a>
	 * <p>
	 * When a ghost is on a portal tile and the steering has just changed, it may happen that no
	 * direction can be computed. In that case we keep the move direction.
	 * <p>
	 * Note: We use separate parameters for the move direction, current and target tile instead of the
	 * members of the actor itself because the {@link #pathTo(Tile)} method uses this method without
	 * actually placing the actor at each tile of the path.
	 * 
	 * @param mover   actor moving through the maze
	 * @param moveDir current move direction
	 * @param tile    current tile
	 * @param target  target tile
	 */
	private Direction bestDirTowardsTarget(MobileLifeform mover, Direction moveDir, Tile tile, Tile target) {
		Function<Direction, Double> fnTargetDistance = dir -> mover.world().neighbor(tile, dir).distance(target);
		Function<Direction, Integer> fnDirectionPriority = directionPriority::indexOf;
		/*@formatter:off*/
		return Direction.dirs()
			.filter(dir -> dir != moveDir.opposite())
			.filter(dir -> mover.canMoveBetween(tile, mover.world().neighbor(tile, dir)))
			.sorted(Comparator.comparing(fnTargetDistance).thenComparing(fnDirectionPriority))
			.findFirst()
			.orElse(mover.moveDir());
		/*@formatter:on*/
	}

	/**
	 * Computes the path the entity would traverse until reaching the target tile, a cycle would occur
	 * or the path would leave the map.
	 */
	private void computePath(Tile target) {
		path.clear();
		Direction dir = mover.moveDir();
		Tile head = mover.tileLocation();
		while (mover.world().includes(head) && !head.equals(target) && !path.contains(head)) {
			path.add(head);
			dir = bestDirTowardsTarget(mover, dir, head, target);
			head = mover.world().neighbor(head, dir);
		}
	}

	@Override
	public void force() {
		forced = true;
	}

	@Override
	public void setPathComputed(boolean computed) {
		if (pathComputed != computed) {
			path.clear();
		}
		pathComputed = computed;
	}

	@Override
	public boolean isPathComputed() {
		return pathComputed;
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