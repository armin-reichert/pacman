package de.amr.games.pacman.actor.behavior.common;

import java.util.Collections;
import java.util.List;

import de.amr.games.pacman.model.Tile;

/**
 * Interface for steering of actors.
 * 
 * @author Armin Reichert
 */
public interface Steering {

	/**
	 * Steers the actor towards its target tile or wherever it should move in its
	 * current state.
	 * 
	 * @param actor the steered actor
	 */
	void steer();

	/**
	 * @return tells if the steering requires the actor to always stay aligned with
	 *         the grid
	 */
	boolean requiresGridAlignment();

	/**
	 * @return the complete path to the target id the implementing class computes it
	 */
	default List<Tile> targetPath() {
		return Collections.emptyList();
	}

	/**
	 * Tells the steering to compute the complete path to the target tile. Steerings
	 * may ignore this.
	 * 
	 * @param b if target path should be computed
	 */
	void enableTargetPathComputation(boolean b);
}