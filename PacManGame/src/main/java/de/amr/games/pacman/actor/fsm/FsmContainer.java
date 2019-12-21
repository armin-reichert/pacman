package de.amr.games.pacman.actor.fsm;

import java.util.function.Consumer;

import de.amr.statemachine.State;
import de.amr.statemachine.StateMachine;

/**
 * This interface is implemented by entities that implement the
 * {@link FsmControlled} interface by delegating to a component implementing
 * that interface.
 * 
 * @author Armin Reichert
 *
 * @param <S> state type of the finite-state machine
 * @param <E> event type of the finite-state machine
 */
public interface FsmContainer<S, E> extends FsmControlled<S, E> {

	/**
	 * The component (delegate) implementing the {@link FsmControlled} interface.
	 * 
	 * @return delegate component
	 */
	FsmControlled<S, E> fsmComponent();

	@Override
	default StateMachine<S, E> fsm() {
		return fsmComponent().fsm();
	}

	@Override
	default void addGameEventListener(Consumer<E> listener) {
		fsmComponent().addGameEventListener(listener);
	}

	@Override
	default void removeGameEventListener(Consumer<E> listener) {
		fsmComponent().removeGameEventListener(listener);
	}

	@Override
	default void setState(S state) {
		fsmComponent().setState(state);
	}

	@Override
	default S getState() {
		return fsmComponent().getState();
	}

	@Override
	@SuppressWarnings("unchecked")
	default boolean is(S... states) {
		return fsmComponent().is(states);
	}

	@Override
	default State<S, E> state() {
		return fsmComponent().state();
	}

	@Override
	default void process(E event) {
		fsmComponent().process(event);
	}
}