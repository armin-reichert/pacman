package de.amr.games.pacman.controller.steering.api;

import java.util.Optional;

import de.amr.games.pacman.model.world.api.MobileLifeform;
import de.amr.games.pacman.model.world.api.Tile;

/**
 * Interface for steering of lifeforms through their world.
 * 
 * @author Armin Reichert
 */
@FunctionalInterface
public interface Steering<M extends MobileLifeform> {

	/**
	 * Steers the lifeform in its current state.
	 * 
	 * @param mover the steered lifeform
	 */
	void steer(M mover);

	/**
	 * Some steerings needs an initial step.
	 */
	default void init() {
	}

	/**
	 * Triggers the steering once, even if its preconditions are not fulfilled.
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
	 * @return tells if the steering requires that the lifeform stays aligned with the grid (default is
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
}