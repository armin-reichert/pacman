package de.amr.games.pacman.actor;

import java.util.Arrays;
import java.util.function.Consumer;

import de.amr.easy.game.view.Controller;
import de.amr.games.pacman.controller.event.PacManGameEvent;
import de.amr.statemachine.State;
import de.amr.statemachine.StateMachine;

/**
 * An actor is an entity which is controlled by a finite-state machine and can register game event
 * listeners for its published game events.
 * 
 * <p>
 * Most methods have a default implementation that delegates to an instance of a default Actor
 * implementation referenced by the entity class.
 * 
 * @author Armin Reichert
 *
 * @param <S>
 *          state label type of the FSM
 */
public interface Actor<S> extends Controller {

	/** Actor implementation (delegate) provided by the entity implementing the Actor interface. */
	Actor<S> actorPart();

	default String name() {
		return actorPart().name();
	}

	default StateMachine<S, PacManGameEvent> fsm() {
		return actorPart().fsm();
	}

	default void activate() {
		actorPart().activate();
	}

	default void deactivate() {
		actorPart().deactivate();
	}

	default boolean isActive() {
		return actorPart().isActive();
	}

	default void addGameEventListener(Consumer<PacManGameEvent> listener) {
		actorPart().addGameEventListener(listener);
	}

	default void removeGameEventListener(Consumer<PacManGameEvent> listener) {
		actorPart().removeGameEventListener(listener);
	}

	default void setState(S state) {
		actorPart().setState(state);
	}

	default S getState() {
		return actorPart().getState();
	}

	@SuppressWarnings("unchecked")
	default boolean oneOf(S... states) {
		return Arrays.stream(states).anyMatch(s -> s.equals(getState()));
	}

	default State<S, PacManGameEvent> state() {
		return actorPart().state();
	}

	default void process(PacManGameEvent event) {
		actorPart().process(event);
	}
}