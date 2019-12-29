package de.amr.games.pacman.view.core;

import static de.amr.easy.game.Application.LOGGER;
import static de.amr.easy.game.Application.app;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;

import de.amr.easy.game.controller.Lifecycle;
import de.amr.easy.game.entity.Entity;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.input.Keyboard.Modifier;
import de.amr.games.pacman.model.Game;

/**
 * Displays the current frame rate and allows toggling the display by pressing "T";
 * 
 * @author Armin Reichert
 */
public class FPSDisplay extends Entity implements Lifecycle {

	@Override
	public void init() {
	}

	@Override
	public void update() {
		int oldClockSpeed = app().clock.getFrequency();
		int newClockSpeed = oldClockSpeed;
		if (Keyboard.keyPressedOnce(KeyEvent.VK_1) || Keyboard.keyPressedOnce(KeyEvent.VK_NUMPAD1)) {
			newClockSpeed = (Game.SPEED_1_FPS);
		}
		else if (Keyboard.keyPressedOnce(KeyEvent.VK_2) || Keyboard.keyPressedOnce(KeyEvent.VK_NUMPAD2)) {
			newClockSpeed = (Game.SPEED_2_FPS);
		}
		else if (Keyboard.keyPressedOnce(KeyEvent.VK_3) || Keyboard.keyPressedOnce(KeyEvent.VK_NUMPAD3)) {
			newClockSpeed = (Game.SPEED_3_FPS);
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

	@Override
	public void draw(Graphics2D g) {
		if (visible()) {
			try (Pen pen = new Pen(g)) {
				pen.color(new Color(200, 200, 200));
				pen.font(new Font(Font.MONOSPACED, Font.BOLD, 8));
				pen.smooth(() -> {
					pen.drawAtPosition(tf.getX(), tf.getY(),
							String.format("%d|%dfps", app().clock.getRenderRate(), app().clock.getFrequency()));
				});
			}
		}
	}
}
