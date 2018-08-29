package de.amr.games.pacman.controller;

import de.amr.statemachine.StateMachine;
import de.amr.statemachine.StateObject;

/**
 * Mixin for state machine controlled game entities.
 * 
 * @author Armin Reichert
 *
 * @param <S>
 *          state identifier type
 * @param <E>
 *          event type
 */
public interface StateMachineClient<S, E> {

	StateMachine<S, E> getStateMachine();

	default S getState() {
		return getStateMachine().currentState();
	}

	default void setState(S state) {
		getStateMachine().setState(state);
	}

	default StateObject<S, E> getStateObject() {
		return getStateMachine().currentStateObject();
	}

	default void processEvent(E e) {
		getStateMachine().process(e);
	}
}
