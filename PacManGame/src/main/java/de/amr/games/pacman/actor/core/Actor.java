package de.amr.games.pacman.actor.core;

import de.amr.easy.game.view.View;
import de.amr.games.pacman.actor.Cast;
import de.amr.games.pacman.controller.event.PacManGameEvent;
import de.amr.games.pacman.model.Game;
import de.amr.statemachine.client.FsmContainer;
import de.amr.statemachine.core.StateMachine;

/**
 * An actor is a maze resident controlled by a finite-state machine.
 * 
 * @author Armin Reichert
 *
 * @param <S> state (identifier) type
 */
public interface Actor<S> extends FsmContainer<S, PacManGameEvent>, View {

	/**
	 * Builds the state machine for this actor.
	 * 
	 * @return state machine
	 */
	StateMachine<S, PacManGameEvent> buildFsm();

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
}