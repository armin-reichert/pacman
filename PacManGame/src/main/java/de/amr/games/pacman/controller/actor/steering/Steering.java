package de.amr.games.pacman.controller.actor.steering;

import java.util.Collections;
import java.util.List;

import de.amr.games.pacman.model.Tile;

/**
 * Interface for steering of actors.
 * 
 * @author Armin Reichert
 */
public interface Steering {

	static String name(Steering s) {
		return s == null ? "no steering" : s.getClass().getSimpleName();
	}

	/**
	 * Steers the actor towards its target tile or wherever it should move in its
	 * current state.
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
	 * @return tells if the steering requires that moving always keeps the actor
	 *         aligned with the grid
	 */
	boolean requiresGridAlignment();

	/**
	 * @return the path from the actor position to the target tile
	 */
	default List<Tile> targetPath() {
		return Collections.emptyList();
	}

	/**
	 * Tells the steering to compute the target path. Steerings may ignore this.
	 * 
	 * @param enabled if target path should be computed
	 */
	default void enableTargetPathComputation(boolean enabled) {
	}
}