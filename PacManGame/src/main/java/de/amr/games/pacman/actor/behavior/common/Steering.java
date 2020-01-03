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
	 */
	void steer();

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
	void enableTargetPathComputation(boolean enabled);
}