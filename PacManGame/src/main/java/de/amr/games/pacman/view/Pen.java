package de.amr.games.pacman.view;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import de.amr.games.pacman.model.Tile;

/**
 * Helper for drawing texts in grid.
 * 
 * @author Armin Reichert
 */
public class Pen {

	private final Graphics2D g;
	private Font font = new Font(Font.DIALOG, Font.PLAIN, 10);
	private Color color = Color.BLUE;

	public Pen(Graphics2D g) {
		this.g = (Graphics2D) g.create();
	}

	public void color(Color c) {
		color = c;
	}

	public void font(Font f) {
		font = f;
	}

	public void fontSize(float size) {
		font = font.deriveFont(size);
	}

	public void smooth(Runnable ops) {
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		ops.run();
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
	}

	public void draw(String s, int col, int row) {
		g.setColor(color);
		g.setFont(font);
		g.drawString(s, col * Tile.SIZE, row * Tile.SIZE);
	}

	public void hcenter(String s, int viewWidth, int row) {
		g.setColor(color);
		g.setFont(font);
		g.drawString(s, (viewWidth - g.getFontMetrics().stringWidth(s)) / 2, row * Tile.SIZE);

	}
}