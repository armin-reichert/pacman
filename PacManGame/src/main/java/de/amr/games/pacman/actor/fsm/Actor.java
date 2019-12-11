package de.amr.games.pacman.actor.fsm;

import de.amr.easy.game.view.Controller;

public interface Actor extends Controller {

	void activate();

	void deactivate();

	boolean isActive();

}