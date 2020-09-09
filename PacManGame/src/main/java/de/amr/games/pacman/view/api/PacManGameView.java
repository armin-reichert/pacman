package de.amr.games.pacman.view.api;

import java.util.ResourceBundle;

import de.amr.easy.game.controller.StateMachineControlled;
import de.amr.easy.game.controller.Lifecycle;
import de.amr.easy.game.view.View;

/**
 * All views of the Pac-Man game are themeable and have a lifecycle.
 * 
 * @author Armin Reichert
 */
public interface PacManGameView extends View, Lifecycle, StateMachineControlled {

	ResourceBundle texts = ResourceBundle.getBundle("texts");

	void setTheme(Theme theme);

	Theme getTheme();
}
