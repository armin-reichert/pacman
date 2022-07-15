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

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import de.amr.easy.game.view.Pen;
import de.amr.games.pacman.lib.Tile;
import de.amr.games.pacman.model.world.api.TiledWorld;
import de.amr.games.pacman.model.world.components.Bed;

public class GridRenderer {

	private Image gridImage;

	public GridRenderer(int width, int height) {
		gridImage = createGridPatternImage(width, height);
	}

	public void renderGrid(Graphics2D g, TiledWorld world) {
		// use fast GC even if smooth rendering is globally enabled
		Graphics2D g2 = (Graphics2D) g.create();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
		g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
		g2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED);
		g2.drawImage(gridImage, 0, 0, null);
		g2.dispose();
		drawBeds(g, world);
	}

	private BufferedImage createGridPatternImage(int cols, int rows) {
		int width = cols * Tile.TS;
		int height = rows * Tile.TS + 1;
		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = img.createGraphics();
		g.setColor(Rendering.GRID_PATTERN[0]);
		g.fillRect(0, 0, width, height);
		for (int row = 0; row < rows; ++row) {
			for (int col = 0; col < cols; ++col) {
				int i = Rendering.patternIndex(col, row);
				if (i != 0) {
					g.setColor(Rendering.GRID_PATTERN[i]);
					g.fillRect(col * Tile.TS, row * Tile.TS, Tile.TS, Tile.TS);
				}
			}
		}
		g.dispose();
		return img;
	}

	public void drawOneWayTiles(Graphics2D g, TiledWorld world) {
		world.oneWayTiles().forEach(oneWay -> Rendering.drawDirectionIndicator(g, Color.WHITE, false, oneWay.dir,
				oneWay.tile.centerX(), oneWay.tile.y()));
	}

	public void drawBeds(Graphics2D g, TiledWorld world) {
		var house = world.house(0);
		if (house.isPresent()) {
			Color[] colors = { Color.RED, Color.CYAN, Color.PINK, Color.ORANGE };
			for (int i = 0; i < 4; ++i) {
				drawBed(g, house.get().bed(i), i + "", colors[i]);
			}
		}
		drawBed(g, world.pacManBed(), "P", Color.YELLOW);
	}

	private void drawBed(Graphics2D g, Bed bed, String text, Color color) {
		int x = bed.center().roundedX() - Tile.TS;
		int y = bed.center().roundedY() - Tile.TS / 2;
		g.setColor(color);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.drawRoundRect(x, y, 2 * Tile.TS - 1, Tile.TS, 2, 2);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
		try (Pen pen = new Pen(g)) {
			pen.turnSmoothRenderingOn();
			pen.color(Color.WHITE);
			pen.font(new Font(Font.MONOSPACED, Font.BOLD, 7));
			pen.drawCentered(text, bed.center().roundedX(), bed.center().roundedY() + Tile.TS + 1);
		}
	}
}