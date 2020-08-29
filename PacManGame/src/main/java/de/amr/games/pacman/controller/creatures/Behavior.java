package de.amr.games.pacman.controller.creatures;

import java.util.stream.Stream;

import de.amr.games.pacman.controller.steering.api.Steering;
import de.amr.statemachine.core.StateMachine;

/**
 * Implemented by entities with a controlled behavior ("artificial intelligence").
 * 
 * @param <STATE> state (identifier) type
 * 
 * @author Armin Reichert
 */
public interface Behavior<STATE> {

	/**
	 * Defines the behavior for the given state.
	 * 
	 * @param state    state
	 * @param steering steering for given state
	 */
	void behavior(STATE state, Steering steering);

	/**
	 * @return state machines controlling this entity
	 */
	Stream<StateMachine<?, ?>> machines();

}