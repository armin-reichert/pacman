package de.amr.games.pacman.actor.core;

import de.amr.games.pacman.controller.event.PacManGameEvent;
import de.amr.statemachine.client.FsmContainer;

/**
 * An actor is a maze resident with an integrated state machine.
 * 
 * @author Armin Reichert
 *
 * @param <S> state identifier type
 */
public interface PacManGameActor<S> extends MazeResident, FsmContainer<S, PacManGameEvent> {

}
