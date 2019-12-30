package de.amr.games.pacman.view.core;

import static de.amr.easy.game.Application.app;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import de.amr.easy.game.controller.Lifecycle;
import de.amr.easy.game.view.View;
import de.amr.games.pacman.theme.Theme;

/**
 * Common interface for all views in Pac-Man.
 * 
 * @author Armin Reichert
 */
public interface GameView extends View, Lifecycle, PropertyChangeListener {

	default int width() {
		return app().settings.width;
	}

	default int height() {
		return app().settings.height;
	}

	Theme theme();

	void onThemeChanged(Theme theme);

	@Override
	default void propertyChange(PropertyChangeEvent e) {
		if ("theme".equals(e.getPropertyName())) {
			Theme newTheme = (Theme) e.getNewValue();
			onThemeChanged(newTheme);
		}
	}
}