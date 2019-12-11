package de.amr.games.pacman.actor.fsm;

import static de.amr.easy.game.Application.LOGGER;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

import de.amr.games.pacman.controller.event.PacManGameEvent;
import de.amr.statemachine.State;
import de.amr.statemachine.StateMachine;

/**
 * Prototypical implementation of the {@link FsmControlled} interface which can
 * be used as a delegate by an entity class.
 * 
 * @author Armin Reichert
 *
 * @param <S> state (label) type of the FSM
 */
public class FsmComponent<S> implements FsmControlled<S> {

	public final String name;
	public final StateMachine<S, PacManGameEvent> fsm;
	public final Set<Consumer<PacManGameEvent>> listeners;
	public Predicate<PacManGameEvent> publishedEventIsLogged;
	private boolean active;

	public FsmComponent(String name, StateMachine<S, PacManGameEvent> fsm) {
		this.name = name;
		this.fsm = fsm;
		active = false;
		publishedEventIsLogged = event -> true;
		listeners = new LinkedHashSet<>();
	}

	@Override
	public StateMachine<S, PacManGameEvent> fsm() {
		return fsm;
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public void activate() {
		active = true;
		LOGGER.info(() -> String.format("Actor '%s' has been activated", name));
	}

	@Override
	public void deactivate() {
		active = false;
		LOGGER.info(() -> String.format("Actor '%s' has been deactivated", name));
	}

	@Override
	public boolean isActive() {
		return active;
	}

	@Override
	public void addGameEventListener(Consumer<PacManGameEvent> listener) {
		listeners.add(listener);
	}

	@Override
	public void removeGameEventListener(Consumer<PacManGameEvent> listener) {
		listeners.remove(listener);
	}

	public void publish(PacManGameEvent event) {
		if (publishedEventIsLogged.test(event)) {
			LOGGER.info(() -> String.format("Actor '%s' published event '%s'", name, event));
		}
		listeners.forEach(listener -> listener.accept(event));
	}

	@Override
	public S getState() {
		return fsm.getState();
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean oneOf(S... states) {
		return Arrays.stream(states).anyMatch(state -> state == getState());
	}

	@Override
	public void setState(S state) {
		fsm.setState(state);
	}

	@Override
	public State<S, PacManGameEvent> state() {
		return fsm.state();
	}

	@Override
	public void process(PacManGameEvent event) {
		fsm.process(event);
	}

	@Override
	public void init() {
		fsm.init();
	}

	@Override
	public void update() {
		fsm.update();
	}
}