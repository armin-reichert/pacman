package de.amr.games.pacman.controller.actor.steering;

/**
 * Interface for steering of actors.
 * 
 * @author Armin Reichert
 */
@FunctionalInterface
public interface Steering {

	/**
	 * Steers the actor towards its target tile or wherever it should move in its current state.
	 */
	void steer();

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