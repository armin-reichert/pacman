package de.amr.games.pacman.view.core;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;

import de.amr.games.pacman.model.tiles.Tile;

/**
 * Helper for drawing texts in grid.
 * 
 * @author Armin Reichert
 */
public class Pen implements AutoCloseable {

	private final Graphics2D g;

	public Pen(Graphics2D g2) {
		g = (Graphics2D) g2.create();
		g.setFont(new Font(Font.DIALOG, Font.PLAIN, 10));
		g.setColor(Color.BLUE);
	}

	@Override
	public void close() {
		g.dispose();
	}

	public void color(Color c) {
		g.setColor(c);
	}

	public void font(Font f) {
		g.setFont(f);
	}

	public FontMetrics getFontMetrics() {
		return g.getFontMetrics();
	}

	public void fontSize(float size) {
		g.setFont(g.getFont().deriveFont(size));
	}

	public void smooth(Runnable ops) {
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		ops.run();
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
	}

	public void drawAtTilePosition(int col, int row, String s) {
		Rectangle2D box = g.getFontMetrics().getStringBounds(s, g);
		float dy = Math.round((Tile.SIZE / 2 + box.getHeight()) / 2);
		g.drawString(s, col * Tile.SIZE, row * Tile.SIZE + dy);
	}

	public void drawString(String s, float x, float y) {
		g.drawString(s, x, y);
	}

	public void hcenter(String s, int containerWidth, int row) {
		float x = (containerWidth - getFontMetrics().stringWidth(s)) / 2, y = row * Tile.SIZE;
		g.drawString(s, x, y);
	}
}