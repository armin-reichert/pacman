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
package de.amr.games.pacman.view.common;

import static java.lang.Math.PI;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;

import de.amr.easy.game.math.Vector2f;
import de.amr.games.pacman.controller.creatures.ghost.Ghost;
import de.amr.games.pacman.model.world.api.Direction;
import de.amr.games.pacman.model.world.api.Tile;

public class Rendering {

	private Rendering() {
	}

	public static final String INFTY = Character.toString('\u221E');

	protected static final Color[] GRID_PATTERN = { Color.BLACK, new Color(40, 40, 40) };

	protected static final Polygon TRIANGLE = new Polygon(new int[] { -4, 4, 0 }, new int[] { 0, 0, 4 }, 3);

	public static void smoothOn(Graphics2D g) {
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
	}

	public static void smoothOff(Graphics2D g) {
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
	}

	public static void drawCircleWithText(Graphics2D g, Vector2f center, int radius, Color color, String text) {
		g.translate(center.x, center.y);
		g.setColor(color);
		g.drawOval(-radius, -radius, 2 * radius, 2 * radius);
		FontMetrics fm = g.getFontMetrics();
		int width = fm.stringWidth(text);
		g.drawString(text, -width / 2, fm.getHeight() / 2);
		g.translate(-center.x, -center.y);
	}

	public static Color alpha(Color color, int alpha) {
		return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
	}

	public static Color ghostColor(Ghost ghost) {
		switch (ghost.name) {
		case "Blinky":
			return Color.RED;
		case "Pinky":
			return Color.PINK;
		case "Inky":
			return Color.CYAN;
		case "Clyde":
			return Color.ORANGE;
		default:
			throw new IllegalArgumentException("Ghost name unknown: " + ghost.name);
		}
	}

	public static int patternIndex(int col, int row) {
		return (col + row) % GRID_PATTERN.length;
	}

	public static Color patternColor(Tile tile) {
		return GRID_PATTERN[patternIndex(tile.col, tile.row)];
	}

	public static void drawDirectionIndicator(Graphics2D g, Color color, boolean fill, Direction dir, int x, int y) {
		g = (Graphics2D) g.create();
		g.setStroke(new BasicStroke(0.1f));
		g.translate(x, y);
		g.rotate((dir.ordinal() - 2) * (PI / 2));
		g.setColor(color);
		if (fill) {
			g.fillPolygon(TRIANGLE);
		} else {
			g.drawPolygon(TRIANGLE);
		}
		g.dispose();
	}
}