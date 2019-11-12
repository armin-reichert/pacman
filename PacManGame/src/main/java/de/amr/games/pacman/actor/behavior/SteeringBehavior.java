package de.amr.games.pacman.actor.behavior;

import de.amr.games.pacman.actor.MazeMover;

/**
 * Steering behavior of actors.
 * 
 * @author Armin Reichert
 */
public interface SteeringBehavior {

	/**
	 * Steers the actor towards its target or wherever it should move in its current state.
	 * 
	 * @param actor
	 *                the moving actor
	 */
	void steer(MazeMover actor);

	/**
	 * TODO: maybe this is obsolete now
	 * 
	 * Computes a path to the current target which can be cached to avoid too many path finder calls.
	 * 
	 * @param actor
	 *                the moving actor
	 */
	default void computePath(MazeMover actor) {
	}
}