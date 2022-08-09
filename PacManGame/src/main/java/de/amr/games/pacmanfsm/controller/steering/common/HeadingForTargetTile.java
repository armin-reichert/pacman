/*
MIT License

Copyright (c) 2019 Armin Reichert

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package de.amr.games.pacmanfsm.controller.steering.common;

import static de.amr.games.pacmanfsm.lib.Direction.DOWN;
import static de.amr.games.pacmanfsm.lib.Direction.LEFT;
import static de.amr.games.pacmanfsm.lib.Direction.RIGHT;
import static de.amr.games.pacmanfsm.lib.Direction.UP;
import static java.util.Comparator.comparing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import de.amr.games.pacmanfsm.controller.creatures.Guy;
import de.amr.games.pacmanfsm.controller.steering.api.Steering;
import de.amr.games.pacmanfsm.lib.Direction;
import de.amr.games.pacmanfsm.lib.Tile;

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
	 * @param guy     the steered guy
	 * @param moveDir current move direction
	 * @param tile    current tile
	 * @param target  target tile
	 */
	private static Direction bestDirTowardsTarget(Guy guy, Direction moveDir, Tile tile, Tile target) {
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

	private final Supplier<Tile> fnTargetTile;
	private List<Tile> path;
	private boolean pathComputed;
	private boolean forced;

	public HeadingForTargetTile(Supplier<Tile> fnTargetTile) {
		this.fnTargetTile = Objects.requireNonNull(fnTargetTile);
		path = Collections.emptyList();
	}

	@Override
	public Optional<Tile> targetTile() {
		return Optional.ofNullable(fnTargetTile.get());
	}

	@Override
	public void steer(Guy guy) {
		if (forced || guy.enteredNewTile) {
			Tile target = fnTargetTile.get();
			if (target != null) {
				guy.wishDir = bestDirTowardsTarget(guy, guy.moveDir, guy.tile(), target);
				updatePath(guy, target);
			}
			forced = false;
		}
	}

	/**
	 * Computes the path the guy would traverse until either reaching the target tile, running into a
	 * cycle or entering a portal.
	 */
	private void updatePath(Guy guy, Tile target) {
		if (target != null) {
			path = new ArrayList<>();
			Direction dir = guy.moveDir;
			Tile next = guy.tile();
			while (!next.equals(target) && guy.world.includes(next) && !path.contains(next)) {
				path.add(next);
				dir = bestDirTowardsTarget(guy, dir, next, target);
				next = guy.world.neighbor(next, dir);
			}
		}
	}

	@Override
	public List<Tile> pathToTarget() {
		return Collections.unmodifiableList(path);
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
		pathComputed = computed;
	}
}