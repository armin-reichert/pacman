package de.amr.games.pacman.view.api;

import de.amr.easy.game.controller.Lifecycle;
import de.amr.easy.game.view.View;

public interface PacManGameView extends View, Lifecycle {

	void setTheme(Theme theme);
}
