package de.amr.games.pacman.controller.steering.common;

import static de.amr.games.pacman.model.world.api.Direction.DOWN;
import static de.amr.games.pacman.model.world.api.Direction.LEFT;
import static de.amr.games.pacman.model.world.api.Direction.RIGHT;
import static de.amr.games.pacman.model.world.api.Direction.UP;
import static java.util.Comparator.comparing;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import de.amr.games.pacman.controller.creatures.SmartGuy;
import de.amr.games.pacman.controller.steering.api.PathProvidingSteering;
import de.amr.games.pacman.model.world.api.Direction;
import de.amr.games.pacman.model.world.api.Tile;
import de.amr.games.pacman.model.world.core.MovingGuy;

/**
 * Steers a guy towards a target tile.
 * 
 * The detailed behavior is described
 * <a href= "http://gameinternals.com/understanding-pac-man-ghost-behavior">here</a>.
 * 
 * @author Armin Reichert
 */
public class HeadingForTargetTile implements PathProvidingSteering {

	private static final List<Direction> DIRECTION_ORDER = List.of(UP, LEFT, DOWN, RIGHT);

	/**
	 * Computes the next move direction as described
	 * <a href= "http://gameinternals.com/understanding-pac-man-ghost-behavior">here.</a>
	 * <p>
	 * Note: I use method parameters for the move direction, current and target tile instead of the
	 * fields of the guy because the {@link #pathTo(Tile)} method also uses this method without actually
	 * moving the guy along the path.
	 * 
	 * @param guy     the guy moving
	 * @param moveDir current move direction
	 * @param tile    current tile
	 * @param target  target tile
	 */
	private static Direction bestDirTowardsTarget(SmartGuy<?> guy, Direction moveDir, Tile tile, Tile target) {
		/*@formatter:off*/
		return Direction.dirs()
			.filter(dir -> dir != moveDir.opposite())
			.filter(dir -> guy.canMoveBetween(tile, guy.world.neighbor(tile, dir)))
			.sorted(comparing((Direction dir) -> guy.world.neighbor(tile, dir).distance(target))
					.thenComparing(DIRECTION_ORDER::indexOf))
			.findFirst()
			.orElse(moveDir);
		/*@formatter:on*/
	}

	private final SmartGuy<?> guy;
	private final List<Tile> path = new ArrayList<>();
	private Supplier<Tile> fnTargetTile;
	private boolean forced;
	private boolean pathComputed;

	public HeadingForTargetTile(SmartGuy<?> guy, Supplier<Tile> fnTargetTile) {
		this.guy = Objects.requireNonNull(guy);
		this.fnTargetTile = Objects.requireNonNull(fnTargetTile);
	}

	@Override
	public Optional<Tile> targetTile() {
		return Optional.ofNullable(fnTargetTile.get());
	}

	@Override
	public void steer(MovingGuy body) {
		if (body.enteredNewTile || forced) {
			forced = false;
			Tile targetTile = fnTargetTile.get();
			if (targetTile != null) {
				body.wishDir = bestDirTowardsTarget(guy, body.moveDir, body.tile(), targetTile);
				if (pathComputed) {
					computePath(targetTile);
				}
			} else {
				path.clear();
			}
		}
	}

	/**
	 * Computes the path the entity would traverse until reaching the target tile, a cycle would occur
	 * or the path would leave the world.
	 */
	private void computePath(Tile target) {
		Direction dir = guy.body.moveDir;
		Tile head = guy.body.tile();
		path.clear();
		while (guy.world.includes(head) && !head.equals(target) && !path.contains(head)) {
			path.add(head);
			dir = bestDirTowardsTarget(guy, dir, head, target);
			head = guy.world.neighbor(head, dir);
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