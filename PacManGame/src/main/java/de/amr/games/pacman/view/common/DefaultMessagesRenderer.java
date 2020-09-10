package de.amr.games.pacman.view.common;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

import de.amr.easy.game.view.Pen;
import de.amr.games.pacman.model.world.components.Tile;
import de.amr.games.pacman.view.api.MessagesRenderer;

public class DefaultMessagesRenderer implements MessagesRenderer {

	private int row;
	private Color textColor;
	private Font font;
	private int fontSize;
	private boolean textAntialiasing;

	public DefaultMessagesRenderer() {
		row = 21;
		textColor = Color.YELLOW;
		font = new Font(Font.MONOSPACED, Font.PLAIN, Tile.SIZE);
		textAntialiasing = false;
	}

	@Override
	public void draw(Graphics2D g, String message, int widthInTiles) {
		if (message != null) {
			try (Pen pen = new Pen(g)) {
				if (textAntialiasing) {
					pen.turnSmoothRenderingOn();
				}
				pen.font(font);
				pen.color(textColor);
				pen.move(0, row * Tile.SIZE);
				pen.hcenter(message, widthInTiles * Tile.SIZE);
			}
		}
	}

	@Override
	public Font getFont() {
		return font;
	}

	@Override
	public void setFont(Font font) {
		this.font = font;
	}

	@Override
	public int getFontSize() {
		return fontSize;
	}

	@Override
	public void setFontSize(int fontSize) {
		this.fontSize = fontSize;
		font = font.deriveFont((float) fontSize);
	}

	@Override
	public void setTextAntialiasing(boolean enabled) {
		this.textAntialiasing = enabled;
	}

	@Override
	public Color getTextColor() {
		return textColor;
	}

	@Override
	public void setTextColor(Color color) {
		this.textColor = color;
	}

	@Override
	public void setRow(int row) {
		this.row = row;
	}
}