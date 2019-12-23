package de.amr.games.pacman.view;

import static de.amr.easy.game.Application.LOGGER;
import static de.amr.easy.game.Application.app;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.input.Keyboard.Modifier;
import de.amr.easy.game.view.Lifecycle;
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

	public AbstractPacManGameView(int width, int height) {
		this.width = width;
		this.height = height;
	}

	public void setShowFrameRate(boolean showFrameRate) {
		this.showFrameRate = showFrameRate;
	}

	private void handleClockSpeedChange() {
		int fps = app().clock.getFrequency();
		if (Keyboard.keyPressedOnce(KeyEvent.VK_1) || Keyboard.keyPressedOnce(KeyEvent.VK_NUMPAD1)) {
			setClockFrequency(PacManGame.SPEED_1_FPS);
		} else if (Keyboard.keyPressedOnce(KeyEvent.VK_2) || Keyboard.keyPressedOnce(KeyEvent.VK_NUMPAD2)) {
			setClockFrequency(PacManGame.SPEED_2_FPS);
		} else if (Keyboard.keyPressedOnce(KeyEvent.VK_3) || Keyboard.keyPressedOnce(KeyEvent.VK_NUMPAD3)) {
			setClockFrequency(PacManGame.SPEED_3_FPS);
		} else if (Keyboard.keyPressedOnce(Modifier.ALT, KeyEvent.VK_LEFT)) {
			setClockFrequency(fps <= 10 ? Math.max(1, fps - 1) : fps - 5);
		} else if (Keyboard.keyPressedOnce(Modifier.ALT, KeyEvent.VK_RIGHT)) {
			setClockFrequency(fps < 10 ? fps + 1 : fps + 5);
		}
	}

	private void setClockFrequency(int ticksPerSecond) {
		app().clock.setFrequency(ticksPerSecond);
		LOGGER.info(() -> String.format("Clock frequency set to %d ticks/sec", ticksPerSecond));
	}

	protected void drawFPS(Graphics2D g) {
		Pen pen = new Pen(g);
		pen.color(new Color(200, 200, 200));
		pen.font(new Font(Font.MONOSPACED, Font.BOLD, 8));
		pen.smooth(() -> {
			pen.draw(String.format("%d|%dfps", app().clock.getRenderRate(), app().clock.getFrequency()), 0, 17);
		});
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
	}

	@Override
	public void init() {
		showFrameRate = false;
	}

	@Override
	public void update() {
		if (Keyboard.keyPressedOnce(KeyEvent.VK_T)) {
			setShowFrameRate(!showFrameRate);
		}
		handleClockSpeedChange();
	}

	public abstract PacManTheme theme();
}