package de.amr.games.pacman.view.core;

import static de.amr.easy.game.Application.LOGGER;
import static de.amr.easy.game.Application.app;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import de.amr.easy.game.Application;
import de.amr.easy.game.controller.Lifecycle;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.input.Keyboard.Modifier;
import de.amr.easy.game.view.View;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.theme.PacManTheme;

/**
 * Base class for views with common functionality.
 * 
 * @author Armin Reichert
 */
public abstract class AbstractPacManGameView implements View, Lifecycle, PropertyChangeListener {

	protected final int width;
	protected final int height;
	protected boolean showFrameRate;

	public abstract PacManTheme theme();

	public abstract void updateTheme(PacManTheme theme);

	public AbstractPacManGameView() {
		this.width = Application.app().settings.width;
		this.height = Application.app().settings.height;
		showFrameRate = false;
	}

	public void showFrameRate(boolean show) {
		this.showFrameRate = show;
	}

	@Override
	public void propertyChange(PropertyChangeEvent e) {
		if ("theme".equals(e.getPropertyName())) {
			PacManTheme newTheme = (PacManTheme) e.getNewValue();
			updateTheme(newTheme);
		}
	}

	@Override
	public void init() {
		showFrameRate = false;
	}

	@Override
	public void update() {
		if (Keyboard.keyPressedOnce(KeyEvent.VK_T)) {
			showFrameRate(!showFrameRate);
		}
		handleClockSpeedChange();
	}

	private void handleClockSpeedChange() {
		int oldClockSpeed = app().clock.getFrequency();
		int newClockSpeed = oldClockSpeed;
		if (Keyboard.keyPressedOnce(KeyEvent.VK_1) || Keyboard.keyPressedOnce(KeyEvent.VK_NUMPAD1)) {
			newClockSpeed = (PacManGame.SPEED_1_FPS);
		}
		else if (Keyboard.keyPressedOnce(KeyEvent.VK_2) || Keyboard.keyPressedOnce(KeyEvent.VK_NUMPAD2)) {
			newClockSpeed = (PacManGame.SPEED_2_FPS);
		}
		else if (Keyboard.keyPressedOnce(KeyEvent.VK_3) || Keyboard.keyPressedOnce(KeyEvent.VK_NUMPAD3)) {
			newClockSpeed = (PacManGame.SPEED_3_FPS);
		}
		else if (Keyboard.keyPressedOnce(Modifier.ALT, KeyEvent.VK_LEFT)) {
			newClockSpeed = (oldClockSpeed <= 10 ? Math.max(1, oldClockSpeed - 1) : oldClockSpeed - 5);
		}
		else if (Keyboard.keyPressedOnce(Modifier.ALT, KeyEvent.VK_RIGHT)) {
			newClockSpeed = (oldClockSpeed < 10 ? oldClockSpeed + 1 : oldClockSpeed + 5);
		}
		if (newClockSpeed != oldClockSpeed) {
			app().clock.setFrequency(newClockSpeed);
			LOGGER.info(String.format("Clock frequency changed to %d ticks/sec", newClockSpeed));
		}
	}

	protected void drawFPS(Graphics2D g) {
		try (Pen pen = new Pen(g)) {
			pen.color(new Color(200, 200, 200));
			pen.font(new Font(Font.MONOSPACED, Font.BOLD, 8));
			pen.smooth(() -> {
				pen.draw(String.format("%d|%dfps", app().clock.getRenderRate(), app().clock.getFrequency()), 0, 17);
			});
		}
	}
}