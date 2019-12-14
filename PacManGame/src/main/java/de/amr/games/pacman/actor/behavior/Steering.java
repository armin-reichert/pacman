package de.amr.games.pacman.actor.behavior;

import de.amr.games.pacman.actor.behavior.common.Steerings;
import de.amr.games.pacman.actor.core.MazeMover;

/**
 * Functional interface for steering of actors.
 * 
 * @param <T> type of steered entity, must implement the {@link MazeMover}
 *            interface
 * 
 * @author Armin Reichert
 */
@FunctionalInterface
public interface Steering<T extends MazeMover> {

	/**
	 * Steers the actor towards its target tile or wherever it should move in its
	 * current state.
	 * 
	 * @param actor the steered actor
	 */
	void steer(T actor);

	/**
	 * Some steerings like {@link Steerings#headingForTargetTile()} need a trigger
	 * before they start working.
	 * 
	 * @param actor the steered actor
	 */
	default void triggerSteering(T actor) {
		actor.setEnteredNewTile();
	}
}