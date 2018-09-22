package de.amr.games.pacman.navigation;

import de.amr.games.pacman.actor.Actor;

/**
 * Navigation behavior of actors.
 * 
 * @author Armin Reichert
 *
 * @param <T>
 *          actor type
 */
public interface ActorNavigation<T extends Actor> {

	/**
	 * Computes the route the actor should take when this method is called.
	 * 
	 * @param actor
	 *                the moving actor
	 * @return the route the actor should take
	 */
	MazeRoute getRoute(T actor);

	/**
	 * Triggers computation of a static path for the actor. The concrete implementation can then store
	 * this path until the actor has reached the target or another decision has to be taken. Using a
	 * static path can save lots of path finder calls.
	 * 
	 * @param actor
	 *                the moving actor
	 */
	default void computePath(T actor) {
	}
}