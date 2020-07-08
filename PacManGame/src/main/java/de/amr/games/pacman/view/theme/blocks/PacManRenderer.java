package de.amr.games.pacman.view.theme.blocks;

import java.awt.Color;
import java.awt.Graphics2D;

import de.amr.games.pacman.controller.actor.PacMan;
import de.amr.games.pacman.controller.actor.PacManState;
import de.amr.games.pacman.model.world.api.World;
import de.amr.games.pacman.model.world.core.Tile;
import de.amr.games.pacman.view.theme.IPacManRenderer;

class PacManRenderer implements IPacManRenderer {

	private final PacMan pacMan;

	public PacManRenderer(World world) {
		this.pacMan = world.population().pacMan();
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
		int size = pacMan.power > 0 ? tiles(2.5) : tiles(2);
		int x = (int) pacMan.tf.x + (pacMan.tf.width - size) / 2;
		int y = (int) pacMan.tf.y + (pacMan.tf.width - size) / 2;
		g.setColor(Color.YELLOW);
		g.fillOval(x, y, size, size);
	}

	private void drawCollapsing(Graphics2D g) {
		int size = tiles(2);
		int x = (int) pacMan.tf.x + (pacMan.tf.width - size) / 2;
		int y = (int) pacMan.tf.y + (pacMan.tf.width - size) / 2;
		g.setColor(Color.YELLOW);
		g.drawOval(x, y, size / 2, size / 2);
	}
}