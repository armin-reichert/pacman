package de.amr.games.pacman.actor.behavior;

import de.amr.games.pacman.actor.MazeMover;

/**
 * Navigation behavior of actors.
 * 
 * @author Armin Reichert
 */
public interface Behavior {

	/**
	 * Directs the actor towards its target or wherever it should move in its current state.
	 * 
	 * @param actor
	 *                the moving actor
	 */
	void direct(MazeMover actor);

	/**
	 * Triggers computation of a static path for the actor. The concrete implementation can then store
	 * this path until the actor has reached the target or another decision has to be taken. Using a
	 * static path can save lots of path finder calls.
	 * 
	 * @param actor
	 *                the moving actor
	 */
	default void computePath(MazeMover actor) {
	}
}