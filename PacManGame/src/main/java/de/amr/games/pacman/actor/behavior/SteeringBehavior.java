package de.amr.games.pacman.actor.behavior;

import de.amr.games.pacman.actor.MazeMover;

/**
 * Functional interface for steering behavior of actors.
 * 
 * @author Armin Reichert
 */
@FunctionalInterface
public interface SteeringBehavior {

	/**
	 * Steers the actor towards its target or wherever it should move in its current state.
	 * 
	 * @param actor
	 *                the moving actor
	 */
	void steer(MazeMover actor);
}