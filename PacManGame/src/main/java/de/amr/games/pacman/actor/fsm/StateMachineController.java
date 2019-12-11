package de.amr.games.pacman.actor.fsm;

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
 * @author Armin Reichert
 *
 * @param <S> state type of the finite-state machine
 */
public interface StateMachineController<S> extends Controller {

	String name();

	StateMachine<S, PacManGameEvent> fsm();

	void activate();

	void deactivate();

	boolean isActive();

	void addGameEventListener(Consumer<PacManGameEvent> listener);

	void removeGameEventListener(Consumer<PacManGameEvent> listener);

	void setState(S state);

	S getState();

	@SuppressWarnings("unchecked")
	default boolean oneOf(S... states) {
		return Arrays.stream(states).anyMatch(s -> s.equals(getState()));
	}

	State<S, PacManGameEvent> state();

	void process(PacManGameEvent event);
}