package de.amr.games.pacman.actor.fsm;

import java.util.function.Consumer;

import de.amr.easy.game.view.Controller;
import de.amr.games.pacman.controller.event.PacManGameEvent;
import de.amr.statemachine.State;
import de.amr.statemachine.StateMachine;

/**
 * This interface is implemented by entities which are controlled by a finite-state machine and can
 * register game event listeners for their published game events.
 * 
 * @author Armin Reichert
 *
 * @param <S>
 *          state type of the finite-state machine
 */
public interface FsmControlled<S> extends Controller {

	/**
	 * @return descriptive name, used by tracing
	 */
	String name();

	/**
	 * @return state machine controlling this entity
	 */
	StateMachine<S, PacManGameEvent> fsm();

	/**
	 * Adds listener for game events,
	 * 
	 * @param listener
	 *                   game event listener
	 */
	void addGameEventListener(Consumer<PacManGameEvent> listener);

	/**
	 * Removes listener for game events,
	 * 
	 * @param listener
	 *                   game event listener
	 */
	void removeGameEventListener(Consumer<PacManGameEvent> listener);

	/**
	 * Sets the new state of this entity. Normally not used directly.
	 * 
	 * @param state
	 *                the new state
	 */
	void setState(S state);

	/**
	 * @return the current state of this entity
	 */
	S getState();

	/**
	 * 
	 * @param states
	 *                 list of states
	 * @return tells if this entity currently is in one of the given states
	 */
	@SuppressWarnings("unchecked")
	boolean is(S... states);

	/**
	 * @return internal state object corresponding to current state. Gives access to timer.
	 */
	State<S, PacManGameEvent> state();

	/**
	 * Lets the controlling state machine immedialtely process the given event.
	 * 
	 * @param event
	 *                game event to process
	 */
	void process(PacManGameEvent event);
}