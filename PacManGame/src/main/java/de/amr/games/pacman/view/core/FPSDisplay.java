package de.amr.games.pacman.view.core;

import static de.amr.easy.game.Application.app;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

import de.amr.easy.game.entity.Entity;

/**
 * Displays the current frame rate.
 * 
 * @author Armin Reichert
 */
public class FPSDisplay extends Entity {

	@Override
	public void draw(Graphics2D g) {
		if (visible()) {
			try (Pen pen = new Pen(g)) {
				pen.color(new Color(200, 200, 200));
				pen.font(new Font(Font.MONOSPACED, Font.BOLD, 8));
				pen.smooth(() -> {
					String text = String.format("%d|%dfps", app().clock().getFrameRate(), app().clock().getFrequency());
					pen.drawString(text, tf.getX(), tf.getY());
				});
			}
		}
	}
}
