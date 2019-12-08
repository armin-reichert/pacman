package de.amr.games.pacman.actor;

import static de.amr.easy.game.Application.LOGGER;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;

import de.amr.games.pacman.controller.event.PacManGameEvent;
import de.amr.statemachine.State;
import de.amr.statemachine.StateMachine;

/**
 * Implementation of the {@link Actor} interface which can be used as a
 * component in a entity class. This is an implementation alternative to
 * subclassing.
 * 
 * @author Armin Reichert
 *
 * @param <S> state (label) type of the FSM
 */
class ActorImpl<S> implements Actor<S> {

	public final String name;
	public final StateMachine<S, PacManGameEvent> fsm;
	public final Set<Consumer<PacManGameEvent>> listeners;
	public boolean active;

	public ActorImpl(String name, StateMachine<S, PacManGameEvent> fsm) {
		this.name = name;
		this.fsm = fsm;
		active = false;
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