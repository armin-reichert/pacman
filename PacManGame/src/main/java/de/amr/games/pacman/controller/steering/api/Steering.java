package de.amr.games.pacman.controller.steering.api;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import de.amr.games.pacman.model.world.api.Tile;
import de.amr.games.pacman.model.world.core.MovingGuy;

/**
 * Interface for steering guys through their world.
 * 
 * @author Armin Reichert
 */
@FunctionalInterface
public interface Steering {

	/**
	 * Steers the guy.
	 * 
	 * @param guy the steered guy
	 */
	void steer(MovingGuy guy);

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