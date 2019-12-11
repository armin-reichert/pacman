package de.amr.games.pacman.actor.fsm;

import java.util.Arrays;
import java.util.function.Consumer;

import de.amr.games.pacman.controller.event.PacManGameEvent;
import de.amr.statemachine.State;
import de.amr.statemachine.StateMachine;

/**
 * This interface is implemented by entities that implement the
 * {@link FsmControlled} interface by delegation to a component implementing
 * that interface.
 * 
 * @author Armin Reichert
 *
 * @param <S> state type of the finite-state machine
 */
public interface FsmContainer<S> extends FsmControlled<S> {

	FsmControlled<S> fsmComponent();

	@Override
	default String name() {
		return fsmComponent().name();
	}

	@Override
	default StateMachine<S, PacManGameEvent> fsm() {
		return fsmComponent().fsm();
	}

	@Override
	default void addGameEventListener(Consumer<PacManGameEvent> listener) {
		fsmComponent().addGameEventListener(listener);
	}

	@Override
	default void removeGameEventListener(Consumer<PacManGameEvent> listener) {
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
	default boolean oneOf(S... states) {
		return Arrays.stream(states).anyMatch(s -> s.equals(getState()));
	}

	@Override
	default State<S, PacManGameEvent> state() {
		return fsmComponent().state();
	}

	@Override
	default void process(PacManGameEvent event) {
		fsmComponent().process(event);
	}
}