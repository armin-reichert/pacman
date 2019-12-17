package de.amr.games.pacman.view;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import de.amr.games.pacman.model.Tile;

public class Pen {

	private Graphics2D g;
	public Font font;
	public Color color;

	public Pen(Graphics2D g) {
		this.g = (Graphics2D) g.create();
	}

	public void aaOn() {
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
	}

	public void aaOff() {
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
	}

	public void text(String s, int col, int row) {
		g.setColor(color);
		g.setFont(font);
		g.drawString(s, col * Tile.SIZE, row * Tile.SIZE);
	}
}