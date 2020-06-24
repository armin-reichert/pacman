package de.amr.games.pacman.view.core;

import static de.amr.easy.game.Application.app;

import de.amr.easy.game.controller.Lifecycle;
import de.amr.easy.game.view.View;
import de.amr.games.pacman.view.theme.Theme;

/**
 * Base class of all views in the game.
 * 
 * @author Armin Reichert
 */
public abstract class BaseView implements Lifecycle, View {

	public final Theme theme;

	public BaseView(Theme theme) {
		this.theme = theme;
	}

	public int width() {
		return app().settings().width;
	}

	public int height() {
		return app().settings().height;
	}
}