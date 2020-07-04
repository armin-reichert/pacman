package de.amr.games.pacman.view.render;

import java.awt.Color;
import java.awt.Graphics2D;

import de.amr.easy.game.view.Pen;
import de.amr.games.pacman.model.world.api.World;
import de.amr.games.pacman.model.world.core.Tile;
import de.amr.games.pacman.view.theme.Theme;

public class TextRenderer {

	private final World world;
	private final Theme theme;

	private Color textColor;
	private int fontSize;
	private int row;

	public TextRenderer(World world, Theme theme) {
		this.world = world;
		this.theme = theme;
		textColor = Color.YELLOW;
		fontSize = 8;
		row = 21;
	}

	public void draw(Graphics2D g, String messageText) {
		if (messageText != null && messageText.trim().length() > 0) {
			try (Pen pen = new Pen(g)) {
				pen.font(theme.fnt_text());
				pen.fontSize(fontSize);
				pen.color(textColor);
				pen.hcenter(messageText, world.width() * Tile.SIZE, row, Tile.SIZE);
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