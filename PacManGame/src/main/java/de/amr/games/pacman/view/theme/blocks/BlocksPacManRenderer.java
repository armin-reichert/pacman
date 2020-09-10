package de.amr.games.pacman.view.theme.blocks;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;

import de.amr.games.pacman.controller.creatures.pacman.PacMan;
import de.amr.games.pacman.controller.creatures.pacman.PacManState;
import de.amr.games.pacman.view.api.PacManRenderer;
import de.amr.games.pacman.view.common.Rendering;

class BlocksPacManRenderer implements PacManRenderer {

	@Override
	public void render(Graphics2D g, PacMan pacMan) {
		if (!pacMan.visible) {
			return;
		}
		Rendering.smoothOn(g);
		PacManState state = pacMan.ai.getState();
		int size = 2 * pacMan.tf.width;
		switch (state) {
		case AWAKE:
		case POWERFUL:
			drawRunning(g, pacMan, size);
			break;
		case IN_BED:
		case SLEEPING:
		case DEAD:
			drawFull(g, pacMan, size);
			break;
		case COLLAPSING:
			drawCollapsed(g, pacMan, size);
			break;
		default:
			throw new IllegalArgumentException("Unknown Pac-Man state" + state);
		}
		Rendering.smoothOff(g);
	}

	private void drawFull(Graphics2D g, PacMan pacMan, int size) {
		int x = (int) pacMan.tf.x + (pacMan.tf.width - size) / 2;
		int y = (int) pacMan.tf.y + (pacMan.tf.width - size) / 2;
		g.setColor(Color.YELLOW);
		g.fillOval(x, y, size, size);
	}

	private void drawRunning(Graphics2D g, PacMan pacMan, int size) {
		int x = (int) pacMan.tf.x + (pacMan.tf.width - size) / 2;
		int y = (int) pacMan.tf.y + (pacMan.tf.width - size) / 2;
		g.setColor(Color.YELLOW);
		g.fillOval(x, y, size, size);
	}

	private void drawCollapsed(Graphics2D g, PacMan pacMan, int size) {
		Stroke stroke = g.getStroke();
		float thickness = 1f;
		g.setColor(Color.YELLOW);
		for (int d = size; d > size / 8; d = d / 2) {
			int x = (int) pacMan.tf.x + (pacMan.tf.width - d) / 2;
			int y = (int) pacMan.tf.y + (pacMan.tf.width - d) / 2;
			g.setStroke(new BasicStroke(thickness));
			g.drawOval(x, y, d, d);
			thickness = thickness * 0.5f;
		}
		g.setStroke(stroke);
	}
}