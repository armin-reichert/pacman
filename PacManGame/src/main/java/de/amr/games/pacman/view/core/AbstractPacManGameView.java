package de.amr.games.pacman.view.core;

import static de.amr.easy.game.Application.app;

import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import de.amr.easy.game.controller.Lifecycle;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.view.View;
import de.amr.games.pacman.theme.PacManTheme;

/**
 * Base class for views with common functionality.
 * 
 * @author Armin Reichert
 */
public abstract class AbstractPacManGameView implements View, Lifecycle, PropertyChangeListener {

	public final FPSView fpsView;

	public AbstractPacManGameView() {
		fpsView = new FPSView();
	}

	public abstract PacManTheme theme();

	public abstract void onThemeChanged(PacManTheme theme);

	public int width() {
		return app().settings.width;
	}

	public int height() {
		return app().settings.height;
	}

	@Override
	public void propertyChange(PropertyChangeEvent e) {
		if ("theme".equals(e.getPropertyName())) {
			PacManTheme newTheme = (PacManTheme) e.getNewValue();
			onThemeChanged(newTheme);
		}
	}

	@Override
	public void init() {
		fpsView.tf.setPosition(0, 0);
		fpsView.hide();
	}

	@Override
	public void update() {
		fpsView.update();
		if (Keyboard.keyPressedOnce(KeyEvent.VK_T)) {
			if (fpsView.visible()) {
				fpsView.hide();
			}
			else {
				fpsView.show();
			}
		}
	}
}