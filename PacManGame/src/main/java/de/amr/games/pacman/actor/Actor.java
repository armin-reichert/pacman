package de.amr.games.pacman.actor;

import java.util.Arrays;
import java.util.function.Consumer;

import de.amr.easy.game.view.Controller;
import de.amr.games.pacman.controller.event.PacManGameEvent;
import de.amr.statemachine.State;
import de.amr.statemachine.StateMachine;

/**
 * An actor is an entity controlled by a finite-state machine and can register game event listeners
 * for its published game events.
 * 
 * @author Armin Reichert
 *
 * @param <S>
 *          state label type of the FSM
 */
public interface Actor<S> extends Controller {

	String name();

	StateMachine<S, PacManGameEvent> fsm();

	void activate();

	void deactivate();

	boolean isActive();

	void addGameEventListener(Consumer<PacManGameEvent> listener);

	void removeGameEventListener(Consumer<PacManGameEvent> listener);

	default void setState(S state) {
		fsm().setState(state);
	}

	default S getState() {
		return fsm().getState();
	}

	@SuppressWarnings("unchecked")
	default boolean oneOf(S... states) {
		return Arrays.stream(states).anyMatch(s -> s.equals(getState()));
	}

	default State<S, PacManGameEvent> state() {
		return fsm().state();
	}

	default void process(PacManGameEvent event) {
		fsm().process(event);
	}
}