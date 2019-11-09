package de.amr.games.pacman.actor;

import static de.amr.easy.game.Application.LOGGER;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;

import de.amr.games.pacman.controller.event.PacManGameEvent;
import de.amr.statemachine.State;
import de.amr.statemachine.StateMachine;

/**
 * Base class for Pac-Man and the ghosts.
 * <p>
 * An entity controlled by a finite-state machine with the capability of registering event handlers
 * for game events and publishing game events.
 * 
 * @author Armin Reichert
 *
 * @param <S>
 *          state (label) type of the finite-state machine of this actor
 */
public abstract class PacManGameActor<S> extends MazeMover {

	public final String name;

	protected StateMachine<S, PacManGameEvent> fsm;

	private final Set<Consumer<PacManGameEvent>> gameEventListeners = new LinkedHashSet<>();

	public PacManGameActor(String name) {
		this.name = name;
	}

	public void addGameEventListener(Consumer<PacManGameEvent> listener) {
		gameEventListeners.add(listener);
	}

	public void removeGameEventListener(Consumer<PacManGameEvent> listener) {
		gameEventListeners.remove(listener);
	}

	public void publishEvent(PacManGameEvent event) {
		LOGGER.info(String.format("%s reports '%s'", name, event));
		gameEventListeners.forEach(listener -> listener.accept(event));
	}

	public S getState() {
		return fsm.getState();
	}

	public void setState(S state) {
		fsm.setState(state);
	}

	public State<S, PacManGameEvent> state() {
		return fsm.state();
	}

	public void processEvent(PacManGameEvent event) {
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