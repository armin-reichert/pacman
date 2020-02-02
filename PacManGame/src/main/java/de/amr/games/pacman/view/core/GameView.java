package de.amr.games.pacman.view.core;

import static de.amr.easy.game.Application.app;

import de.amr.easy.game.controller.Lifecycle;
import de.amr.easy.game.view.View;

/**
 * Common interface for all views in Pac-Man.
 * 
 * @author Armin Reichert
 */
public interface GameView extends View, Lifecycle {

	default int width() {
		return app().settings().width;
	}

	default int height() {
		return app().settings().height;
	}
}