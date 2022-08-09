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
package de.amr.games.pacmanfsm.theme.blocks;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import de.amr.easy.game.Application;
import de.amr.games.pacmanfsm.controller.creatures.ghost.Ghost;
import de.amr.games.pacmanfsm.controller.creatures.ghost.GhostState;
import de.amr.games.pacmanfsm.theme.api.GhostRenderer;
import de.amr.games.pacmanfsm.view.common.Rendering;

/**
 * @author Armin Reichert
 */
class BlocksGhostRenderer implements GhostRenderer {

	@Override
	public void render(Graphics2D g, Ghost ghost) {
		if (!ghost.visible) {
			return;
		}
		GhostState state = ghost.ai.getState();
		if (state == null) {
			state = GhostState.CHASING;
		}
		Rendering.smoothOn(g);
		int width = 2 * ghost.tf.width - 4;
		int height = 2 * ghost.tf.height - 2;
		switch (state) {
		case CHASING, SCATTERING, LOCKED, LEAVING_HOUSE -> drawColored(g, ghost, width, height, 0, 0);
		case FRIGHTENED -> {
			if (ghost.recovering) {
				var tick = Application.app().clock().getTotalTicks();
				drawFlashing(g, ghost, tick, width, height, 0, 0);
			} else {
				drawFrightened(g, ghost, width, height, 0, 0);
			}
		}
		case DEAD, ENTERING_HOUSE -> {
			if (ghost.bounty > 0) {
				drawPoints(g, ghost);
			} else {
				drawEyes(g, ghost, ghost.tf.width, ghost.tf.width);
			}
		}
		default -> {
			// empty
		}
		}
		Rendering.smoothOff(g);
	}

	private void drawEyes(Graphics2D g, Ghost ghost, int width, int height) {
		int x = centerOffsetX(ghost, width);
		int y = centerOffsetY(ghost, height);
		g.setColor(BlocksTheme.THEME.ghostColor(ghost));
		g.drawRect(x, y, width, height);
	}

	private void drawPoints(Graphics2D g, Ghost ghost) {
		g.setColor(Color.GREEN);
		Font font = BlocksTheme.THEME.asFont("font").deriveFont((float) ghost.tf.height);
		g.setFont(font);
		FontMetrics fm = g.getFontMetrics();
		String text = String.valueOf(ghost.bounty);
		Rectangle2D bounds = fm.getStringBounds(text, g);
		int x = centerOffsetX(ghost, (int) bounds.getWidth());
		int y = centerOffsetY(ghost, (int) bounds.getHeight()) + (int) bounds.getHeight();
		g.drawString(text, x, y);
	}

	private void drawFrightened(Graphics2D g, Ghost ghost, int width, int height, int offsetX, int offsetY) {
		drawShape(g, ghost, width, height, offsetX, offsetY, Color.BLUE);
	}

	private void drawFlashing(Graphics2D g, Ghost ghost, long tick, int width, int height, int offsetX, int offsetY) {
		boolean flash = tick % 30 < 15;
		drawShape(g, ghost, width, height, offsetX, offsetY, flash ? Color.WHITE : Color.BLUE);
	}

	private void drawColored(Graphics2D g, Ghost ghost, int width, int height, int offsetX, int offsetY) {
		drawShape(g, ghost, width, height, offsetX, offsetY, BlocksTheme.THEME.ghostColor(ghost));
	}

	private void drawShape(Graphics2D g, Ghost ghost, int width, int height, int offsetX, int offsetY, Color color) {
		int x = centerOffsetX(ghost, width) + offsetX;
		int y = centerOffsetY(ghost, height) + offsetY;
		g.setColor(color);
		g.fillRect(x, y, width, height);
		g.fillArc(x, y - height / 4 - 2, width, height, 0, 180);
		g.setColor(Color.BLACK);
		g.fillRect(x, y, width, 2);
		g.setColor(Color.WHITE);
		g.fillRect(x, y + height - 2, width, 2);
	}

	private int centerOffsetX(Ghost ghost, int width) {
		return (int) ghost.tf.x + (ghost.tf.width - width) / 2;
	}

	private int centerOffsetY(Ghost ghost, int height) {
		return (int) ghost.tf.y + (ghost.tf.height - height) / 2;
	}
}