package de.amr.games.pacman.view.core;

import static de.amr.easy.game.Application.app;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import de.amr.easy.game.controller.Lifecycle;
import de.amr.easy.game.view.View;
import de.amr.games.pacman.theme.Theme;

/**
 * Base class for views with common functionality.
 * 
 * @author Armin Reichert
 */
public abstract class PacManGameView implements View, Lifecycle, PropertyChangeListener {

	public final FPSView fpsView;

	public PacManGameView() {
		fpsView = new FPSView();
	}

	public abstract Theme theme();

	public abstract void onThemeChanged(Theme theme);

	public int width() {
		return app().settings.width;
	}

	public int height() {
		return app().settings.height;
	}

	@Override
	public void propertyChange(PropertyChangeEvent e) {
		if ("theme".equals(e.getPropertyName())) {
			Theme newTheme = (Theme) e.getNewValue();
			onThemeChanged(newTheme);
		}
	}

	@Override
	public void init() {
		fpsView.init();
	}

	@Override
	public void update() {
		fpsView.update();
	}
}