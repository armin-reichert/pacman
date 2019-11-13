package de.amr.games.pacman.actor.behavior;

import de.amr.games.pacman.actor.MazeMover;

/**
 * Functional interface for steering of actors.
 * 
 * @author Armin Reichert
 */
@FunctionalInterface
public interface Steering {

	/**
	 * Steers the actor towards its target tile or wherever it should move in its
	 * current state.
	 * 
	 * @param actor the steered actor
	 */
	void steer(MazeMover actor);
}