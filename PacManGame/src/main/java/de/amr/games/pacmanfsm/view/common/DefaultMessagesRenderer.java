/*
MIT License

Copyright (c) 2019 Armin Reichert

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package de.amr.games.pacmanfsm.view.common;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

import de.amr.easy.game.view.Pen;
import de.amr.games.pacmanfsm.lib.Tile;
import de.amr.games.pacmanfsm.theme.api.MessagesRenderer;

public class DefaultMessagesRenderer implements MessagesRenderer {

	private int row;
	private Color textColor;
	private Font font;
	private int fontSize;
	private boolean textAntialiasing;

	public DefaultMessagesRenderer() {
		row = 21;
		textColor = Color.YELLOW;
		font = new Font(Font.MONOSPACED, Font.PLAIN, Tile.TS);
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
				pen.move(0, row * Tile.TS);
				pen.hcenter(message, widthInTiles * Tile.TS);
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