package de.amr.games.pacman.view.theme.common;

import static java.lang.Math.PI;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;

import de.amr.games.pacman.controller.actor.Ghost;
import de.amr.games.pacman.model.Direction;
import de.amr.games.pacman.model.world.core.Tile;

public class Rendering {

	public static final String INFTY = Character.toString('\u221E');
	public static final Color[] GRID_PATTERN = { Color.BLACK, new Color(40, 40, 40) };

	private static final Polygon TRIANGLE = new Polygon(new int[] { -4, 4, 0 }, new int[] { 0, 0, 4 }, 3);

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
