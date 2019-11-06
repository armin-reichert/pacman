package de.amr.games.pacman.actor;

import static de.amr.easy.game.Application.LOGGER;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;

import de.amr.games.pacman.controller.event.PacManGameEvent;
import de.amr.games.pacman.model.PacManGame;
import de.amr.statemachine.State;
import de.amr.statemachine.StateMachine;

/**
 * Maze mover controlled by a finite state machine and with the capability of registering event
 * handlers for game events.
 * 
 * @author Armin Reichert
 *
 * @param <S>
 *          state label type of FSM
 * @param <E>
 *          event type of FSM
 */
public abstract class MazeMoverUsingFSM<S, E> extends MazeMover {

	protected StateMachine<S, E> fsm;

	/* Event listeners. */
	private final Set<Consumer<PacManGameEvent>> gameEventListeners = new LinkedHashSet<>();

	public MazeMoverUsingFSM(PacManGame game, String name) {
		super(game, name);
	}

	/**
	 * Adds a listener for the game events published by this maze mover.
	 * 
	 * @param listener
	 *                   event listener
	 */
	public void addGameEventListener(Consumer<PacManGameEvent> listener) {
		gameEventListeners.add(listener);
	}

	/**
	 * Removes the given game event listener.
	 * 
	 * @param listener
	 *                   event listener
	 */
	public void removeGameEventListener(Consumer<PacManGameEvent> listener) {
		gameEventListeners.remove(listener);
	}

	/**
	 * Publishes the given event and informs all registered listeners.
	 * 
	 * @param event
	 *                a game event
	 */
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

	public State<S, E> state() {
		return fsm.state();
	}

	public void processEvent(E event) {
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