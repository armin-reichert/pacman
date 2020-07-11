package de.amr.games.pacman.view.theme.blocks;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;

import de.amr.games.pacman.controller.actor.PacMan;
import de.amr.games.pacman.controller.actor.PacManState;
import de.amr.games.pacman.model.world.core.Tile;
import de.amr.games.pacman.view.api.IPacManRenderer;

class PacManRenderer implements IPacManRenderer {

	private final PacMan pacMan;

	public PacManRenderer(PacMan pacMan) {
		this.pacMan = pacMan;
	}

	@Override
	public void render(Graphics2D g) {
		if (!pacMan.visible) {
			return;
		}
		smoothDrawingOn(g);
		PacManState state = pacMan.getState();
		switch (state) {
		case DEAD:
			drawCollapsing(g);
			break;
		case RUNNING:
			drawRunning(g);
			break;
		case SLEEPING:
			drawFull(g);
			break;
		default:
			break;
		}
		smoothDrawingOff(g);
	}

	private int tiles(double amount) {
		return (int) (amount * Tile.SIZE);
	}

	private void drawFull(Graphics2D g) {
		int size = tiles(2);
		int x = (int) pacMan.tf.x + (pacMan.tf.width - size) / 2;
		int y = (int) pacMan.tf.y + (pacMan.tf.width - size) / 2;
		g.setColor(Color.YELLOW);
		g.fillOval(x, y, size, size);
	}

	private void drawRunning(Graphics2D g) {
		int size = pacMan.getPower() > 0 ? tiles(2.5) : tiles(2);
		int x = (int) pacMan.tf.x + (pacMan.tf.width - size) / 2;
		int y = (int) pacMan.tf.y + (pacMan.tf.width - size) / 2;
		g.setColor(Color.YELLOW);
		g.fillOval(x, y, size, size);
	}

	private void drawCollapsing(Graphics2D g) {
		Stroke stroke = g.getStroke();
		float thickness = 1f;
		g.setColor(Color.YELLOW);
		for (int d = tiles(2); d > tiles(0.25f); d = d / 2) {
			int x = (int) pacMan.tf.x + (pacMan.tf.width - d) / 2;
			int y = (int) pacMan.tf.y + (pacMan.tf.width - d) / 2;
			g.setStroke(new BasicStroke(thickness));
			g.drawOval(x, y, d, d);
			thickness = thickness * 0.5f;
		}
		g.setStroke(stroke);
	}
}