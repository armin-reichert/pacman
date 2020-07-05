package de.amr.games.pacman.view.render.sprite;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.view.Pen;
import de.amr.games.pacman.model.world.core.Tile;

public class MessagesRenderer {

	private Color textColor;
	private Font font;
	private int fontSize;
	private int row;

	public MessagesRenderer() {
		font = Assets.font("font.hud");
		textColor = Color.YELLOW;
		fontSize = 8;
		row = 21;
	}

	public void drawCentered(Graphics2D g, String text, int widthInTiles) {
		if (text != null) {
			try (Pen pen = new Pen(g)) {
				pen.font(font);
				pen.fontSize(fontSize);
				pen.color(textColor);
				pen.hcenter(text, widthInTiles * Tile.SIZE, row, Tile.SIZE);
			}
		}
	}

	public void setTextColor(Color textColor) {
		this.textColor = textColor;
	}

	public void setFontSize(int fontSize) {
		this.fontSize = fontSize;
	}

	public void setRow(int row) {
		this.row = row;
	}
}