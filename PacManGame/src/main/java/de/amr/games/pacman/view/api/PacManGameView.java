package de.amr.games.pacman.view.api;

import de.amr.easy.game.controller.Lifecycle;
import de.amr.easy.game.view.View;
import de.amr.games.pacman.view.theme.api.Theme;

/**
 * All views of the Pac-Man game are themeable and have a lifecycle.
 * 
 * @author Armin Reichert
 */
public interface PacManGameView extends View, Lifecycle {

	void setTheme(Theme theme);

	Theme getTheme();
}
