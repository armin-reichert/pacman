package de.amr.games.pacman.view.theme.common;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

import de.amr.easy.game.view.Pen;
import de.amr.games.pacman.model.world.core.Tile;
import de.amr.games.pacman.view.theme.IRenderer;

public class MessagesRenderer implements IRenderer {

	private int row;
	private Color textColor;
	private Font font;

	public MessagesRenderer() {
		row = 21;
		textColor = Color.YELLOW;
		font = new Font(Font.MONOSPACED, Font.PLAIN, Tile.SIZE);
	}

	@Override
	public void render(Graphics2D g) {
	}

	public void drawCentered(Graphics2D g, String text, int widthInTiles) {
		if (text != null) {
			try (Pen pen = new Pen(g)) {
				pen.font(font);
				pen.color(textColor);
				pen.hcenter(text, widthInTiles * Tile.SIZE, row, Tile.SIZE);
			}
		}
	}

	public void setFont(Font font) {
		this.font = font;
	}

	public void setTextColor(Color color) {
		this.textColor = color;
	}

	public void setRow(int row) {
		this.row = row;
	}
}