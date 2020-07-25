package de.amr.games.pacman.controller.steering.api;

import de.amr.games.pacman.model.world.api.MobileLifeform;

/**
 * Interface for steering of actors.
 * 
 * @author Armin Reichert
 */
@FunctionalInterface
public interface Steering<M extends MobileLifeform> {

	/**
	 * Steers the actor towards its target tile or wherever it should move in its current state.
	 * 
	 * @param mover the steered lifeform
	 */
	void steer(M mover);

	/**
	 * Some steerings needs to be initialized.
	 */
	default void init() {
	}

	/**
	 * Triggers the steering once, even if its normal preconditions are not fulfilled.
	 */
	default void force() {
	}

	/**
	 * Tells if the steering has competed. The default is {@code false}.
	 * 
	 * @return if steering has completed.
	 */
	default boolean isComplete() {
		return false;
	}

	/**
	 * @return tells if the steering requires that moving always keeps the actor aligned with the grid.
	 *         The default is {@code false}.
	 */
	default boolean requiresGridAlignment() {
		return false;
	}
}