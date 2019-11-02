package de.amr.games.pacman.actor.behavior;

import de.amr.games.pacman.actor.MazeMover;

/**
 * Navigation behavior of actors.
 * 
 * @author Armin Reichert
 */
public interface Behavior {

	/**
	 * Computes the route the actor should take when this method is called.
	 * 
	 * @param actor
	 *                the moving actor
	 * @return the route the actor should take
	 */
	Route getRoute(MazeMover actor);

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