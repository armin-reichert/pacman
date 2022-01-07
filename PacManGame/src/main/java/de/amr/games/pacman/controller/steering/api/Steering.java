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
package de.amr.games.pacman.controller.steering.api;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import de.amr.games.pacman.controller.creatures.Guy;
import de.amr.games.pacman.model.world.api.Tile;

/**
 * Interface for steering guys through their world.
 * 
 * @author Armin Reichert
 */
@FunctionalInterface
public interface Steering {

	static Steering STANDING_STILL = new Steering() {

		@Override
		public void steer(Guy<?> guy) {
		}
	};

	/**
	 * Steers the guy.
	 * 
	 * @param guy the steered guy
	 */
	void steer(Guy<?> guy);

	/**
	 * Some steerings needs an initial step.
	 */
	default void init() {
	}

	/**
	 * Triggers this steering once, even if preconditions (e.g. that a new tile has been entered) are
	 * not fulfilled.
	 */
	default void force() {
	}

	/**
	 * Some steerings have a defined end state.
	 * 
	 * @return tells if the steering is complete (default: {@code false}).
	 */
	default boolean isComplete() {
		return false;
	}

	/**
	 * @return tells if the steering requires that the mover stays aligned with the grid (default is
	 *         {@code false}).
	 */
	default boolean requiresGridAlignment() {
		return false;
	}

	/**
	 * Steerings may have a dedicated target tile.
	 * 
	 * @return the optional target tile of this steering
	 */
	default Optional<Tile> targetTile() {
		return Optional.empty();
	}

	/**
	 * @return the path from the current position of the mover to its current target tile
	 */
	default List<Tile> pathToTarget() {
		return Collections.emptyList();
	}

	/**
	 * @param enabled if {@code true} the steering computes the path to the target
	 */
	default void setPathComputed(boolean enabled) {
	}

	/**
	 * @return tells if the steering computes the complete path to the target
	 */
	default boolean isPathComputed() {
		return false;
	}
}