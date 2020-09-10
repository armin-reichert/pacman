package de.amr.games.pacman.view.api;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

public interface MessagesRenderer {

	void draw(Graphics2D g, String message, int widthInTiles);

	void setRow(int row);

	Color getTextColor();

	void setTextColor(Color color);

	Font getFont();

	void setFont(Font font);

	int getFontSize();

	void setFontSize(int fontSize);

	void setTextAntialiasing(boolean enabled);
}