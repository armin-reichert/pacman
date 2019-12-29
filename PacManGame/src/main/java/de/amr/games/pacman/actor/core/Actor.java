package de.amr.games.pacman.actor.core;

import de.amr.games.pacman.actor.Cast;
import de.amr.games.pacman.controller.event.PacManGameEvent;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.Maze;
import de.amr.statemachine.client.FsmComponent;
import de.amr.statemachine.client.FsmContainer;
import de.amr.statemachine.core.StateMachine;

/**
 * An actor is a maze resident with a brain (finite state machine).
 * 
 * @author Armin Reichert
 *
 * @param <S> state identifier type
 */
public interface Actor<S> extends MazeResident, FsmContainer<S, PacManGameEvent> {

	/**
	 * @return the cast this actor belongs to.
	 */
	Cast cast();

	/**
	 * @return the game this actor takes part.
	 */
	default Game game() {
		return cast().game();
	}

	/**
	 * @return the maze this actor is residing in..
	 */
	@Override
	default Maze maze() {
		return cast().maze();
	}

	/**
	 * Builds the state machine for this actor.
	 * 
	 * @return state machine
	 */
	StateMachine<S, PacManGameEvent> buildFsm();

	/**
	 * Boilerplate code for creating the actor's brain.
	 * 
	 * @return the brain (state machine component)
	 */
	default FsmComponent<S, PacManGameEvent> buildBrain() {
		return new FsmComponent<>(buildFsm());
	}
}
