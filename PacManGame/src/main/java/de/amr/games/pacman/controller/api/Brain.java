package de.amr.games.pacman.controller.api;

import de.amr.games.pacman.controller.event.PacManGameEvent;
import de.amr.statemachine.api.FsmContainer;

public interface Brain<STATE> extends FsmContainer<STATE, PacManGameEvent> {

}
