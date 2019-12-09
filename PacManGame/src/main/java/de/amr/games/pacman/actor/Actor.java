package de.amr.games.pacman.actor;

import java.util.Arrays;
import java.util.function.Consumer;

import de.amr.easy.game.view.Controller;
import de.amr.games.pacman.controller.event.PacManGameEvent;
import de.amr.statemachine.State;
import de.amr.statemachine.StateMachine;

/**
 * This interface is implemented by entities which are controlled by a
 * finite-state machine and can register game event listeners for its published
 * game events.
 * 
 * <p>
 * Most methods have a default implementation that delegates to an instance of
 * an Actor prototype referenced by the entity class.
 * 
 * @author Armin Reichert
 *
 * @param <S> state type of the finite-state machine
 */
public interface Actor<S> extends Controller {

	/**
	 * Actor prototype referenced the entity implementing the Actor interface.
	 */
	Actor<S> _actor();

	default String name() {
		return _actor().name();
	}

	default StateMachine<S, PacManGameEvent> fsm() {
		return _actor().fsm();
	}

	default void activate() {
		_actor().activate();
	}

	default void deactivate() {
		_actor().deactivate();
	}

	default boolean isActive() {
		return _actor().isActive();
	}

	default void addGameEventListener(Consumer<PacManGameEvent> listener) {
		_actor().addGameEventListener(listener);
	}

	default void removeGameEventListener(Consumer<PacManGameEvent> listener) {
		_actor().removeGameEventListener(listener);
	}

	default void setState(S state) {
		_actor().setState(state);
	}

	default S getState() {
		return _actor().getState();
	}

	@SuppressWarnings("unchecked")
	default boolean oneOf(S... states) {
		return Arrays.stream(states).anyMatch(s -> s.equals(getState()));
	}

	default State<S, PacManGameEvent> state() {
		return _actor().state();
	}

	default void process(PacManGameEvent event) {
		_actor().process(event);
	}
}