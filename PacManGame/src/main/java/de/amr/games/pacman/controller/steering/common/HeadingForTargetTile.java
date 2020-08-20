package de.amr.games.pacman.controller.steering.common;

import static de.amr.games.pacman.model.world.api.Direction.DOWN;
import static de.amr.games.pacman.model.world.api.Direction.LEFT;
import static de.amr.games.pacman.model.world.api.Direction.RIGHT;
import static de.amr.games.pacman.model.world.api.Direction.UP;
import static java.util.Comparator.comparing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import de.amr.games.pacman.controller.creatures.SmartGuy;
import de.amr.games.pacman.controller.steering.api.Steering;
import de.amr.games.pacman.model.world.api.Direction;
import de.amr.games.pacman.model.world.components.Tile;
import de.amr.games.pacman.model.world.core.MovingGuy;

/**
 * Steers a guy towards a target tile.
 * 
 * The detailed behavior is described
 * <a href= "http://gameinternals.com/understanding-pac-man-ghost-behavior">here</a>.
 * 
 * @author Armin Reichert
 */
public class HeadingForTargetTile implements Steering {

	private static final List<Direction> DIRECTION_ORDER = List.of(UP, LEFT, DOWN, RIGHT);

	/**
	 * Computes the next direction to take for reaching the target tile as described
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
	private final Supplier<Tile> fnTargetTile;
	private final List<Tile> path;

	private boolean pathComputed;
	private boolean forced;

	public HeadingForTargetTile(SmartGuy<?> guy, Supplier<Tile> fnTargetTile) {
		this.guy = Objects.requireNonNull(guy);
		this.fnTargetTile = Objects.requireNonNull(fnTargetTile);
		this.path = new ArrayList<>();
	}

	@Override
	public Optional<Tile> targetTile() {
		return Optional.ofNullable(fnTargetTile.get());
	}

	@Override
	public void steer(MovingGuy body) {
		if (forced || body.enteredNewTile) {
			Tile target = fnTargetTile.get();
			if (target != null) {
				body.wishDir = bestDirTowardsTarget(guy, body.moveDir, body.tile(), target);
				updatePath(target);
			}
			forced = false;
		}
	}

	@Override
	public boolean requiresGridAlignment() {
		return true;
	}

	@Override
	public void force() {
		forced = true;
	}

	@Override
	public boolean isPathComputed() {
		return pathComputed;
	}

	@Override
	public void setPathComputed(boolean computed) {
		if (pathComputed != computed) {
			pathComputed = computed;
			updatePath(fnTargetTile.get());
		}
	}

	@Override
	public List<Tile> pathToTarget() {
		return Collections.unmodifiableList(path);
	}

	/**
	 * Computes the path the guy would traverse until either reaching the target tile, running into a
	 * cycle or entering a portal.
	 */
	private void updatePath(Tile target) {
		if (pathComputed) {
			path.clear();
			Direction dir = guy.body.moveDir;
			Tile next = guy.body.tile();
			while (!next.equals(target) && guy.world.includes(next) && !path.contains(next)) {
				path.add(next);
				dir = bestDirTowardsTarget(guy, dir, next, target);
				next = guy.world.neighbor(next, dir);
			}
		}
	}
}