package de.amr.games.pacman.controller.steering.common;

import static de.amr.games.pacman.model.world.api.Direction.DOWN;
import static de.amr.games.pacman.model.world.api.Direction.LEFT;
import static de.amr.games.pacman.model.world.api.Direction.RIGHT;
import static de.amr.games.pacman.model.world.api.Direction.UP;
import static java.util.Arrays.asList;
import static java.util.Comparator.comparing;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Function;
import java.util.function.Supplier;

import de.amr.games.pacman.controller.steering.api.PathProvidingSteering;
import de.amr.games.pacman.model.world.api.Direction;
import de.amr.games.pacman.model.world.api.MobileLifeform;
import de.amr.games.pacman.model.world.api.Tile;
import de.amr.games.pacman.model.world.api.World;

/**
 * Steers an actor towards a target tile.
 * 
 * The detailed behavior is described
 * <a href= "http://gameinternals.com/understanding-pac-man-ghost-behavior">here</a>.
 * 
 * @author Armin Reichert
 */
public class HeadingForTargetTile implements PathProvidingSteering {

	private static final List<Direction> dirSearchOrder = asList(UP, LEFT, DOWN, RIGHT);

	private final MobileLifeform creature;
	private final Supplier<Tile> fnTargetTile;
	private final ConcurrentLinkedDeque<Tile> path = new ConcurrentLinkedDeque<>();
	private boolean forced;
	private boolean pathComputed;

	public HeadingForTargetTile(MobileLifeform creature, Supplier<Tile> fnTargetTile) {
		this.creature = Objects.requireNonNull(creature);
		this.fnTargetTile = Objects.requireNonNull(fnTargetTile);
	}

	@Override
	public void steer() {
		if (creature.enteredNewTile() || forced) {
			forced = false;
			creature.setTargetTile(fnTargetTile.get());
			if (creature.targetTile() != null) {
				creature.setWishDir(bestDir(creature, creature.moveDir(), creature.location(), creature.targetTile()));
				if (pathComputed) {
					computePath();
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
	 * Note: We use separate parameters for the actor's move direction, current tile and target tile
	 * instead of the members of the actor itself because the {@link #pathTo(Tile)} method uses this
	 * method without actually placing the actor at each tile of the path.
	 * 
	 * @param creature actor moving through the maze
	 * @param moveDir  current move direction
	 * @param tile     current tile
	 * @param target   target tile
	 */
	private Direction bestDir(MobileLifeform creature, Direction moveDir, Tile tile, Tile target) {
		World world = creature.world();
		Function<Direction, Double> fnNeighborDistToTarget = dir -> world.neighbor(tile, dir).distance(target);
		/*@formatter:off*/
		return Direction.dirs()
			.filter(dir -> dir != moveDir.opposite())
			.filter(dir -> creature.canMoveBetween(tile, world.neighbor(tile, dir)))
			.sorted(comparing(fnNeighborDistToTarget).thenComparingInt(dirSearchOrder::indexOf))
			.findFirst()
			.orElse(creature.moveDir());
		/*@formatter:on*/
	}

	/**
	 * Computes the path the entity would traverse until reaching the target tile, a cycle would occur
	 * or the path would leave the map.
	 */
	private void computePath() {
		World world = creature.world();
		Tile currentTile = creature.location(), targetTile = creature.targetTile();
		Direction currentDir = creature.moveDir();
		path.clear();
		path.add(currentTile);
		while (!currentTile.equals(targetTile)) {
			Direction dir = bestDir(creature, currentDir, currentTile, targetTile);
			Tile nextTile = world.neighbor(currentTile, dir);
			if (!world.includes(nextTile) || path.contains(nextTile)) {
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