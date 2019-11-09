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
 * Maze mover controlled by a finite-state machine and with the capability of
 * registering event handlers for game events.
 * 
 * @author Armin Reichert
 *
 * @param <S> state label type of FSM
 * @param <E> event type of FSM
 */
public abstract class MazeMoverUsingFSM<S, E> extends MazeMover {

	protected StateMachine<S, E> fsm;
	public final String name;
	private final Set<Consumer<PacManGameEvent>> gameEventListeners = new LinkedHashSet<>();

	public MazeMoverUsingFSM(PacManGame game, String name) {
		super(game);
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