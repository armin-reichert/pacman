package de.amr.games.pacman.controller.actor.steering;

/**
 * Interface for steering of actors.
 * 
 * @author Armin Reichert
 */
public interface Steering {

	/**
	 * A do-nothing steering ("null object pattern").
	 */
	static Steering NONE = new Steering() {

		@Override
		public void steer() {
		}

		@Override
		public boolean requiresGridAlignment() {
			return false;
		}
	};

	static String name(Steering s) {
		return s == null ? "no steering" : s.getClass().getSimpleName();
	}

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
	 * Triggers the steering once even if the precondition is not fulfilled.
	 */
	default void force() {
	}

	/**
	 * Some steerings have an explicit final state.
	 * 
	 * @return if steering reached its final state
	 */
	default boolean isComplete() {
		return false;
	}

	/**
	 * @return tells if the steering requires that moving always keeps the actor aligned with the grid
	 */
	default boolean requiresGridAlignment() {
		return false;
	}
}