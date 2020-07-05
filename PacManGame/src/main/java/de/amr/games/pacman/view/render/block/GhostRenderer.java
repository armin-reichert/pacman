package de.amr.games.pacman.view.render.block;

import java.awt.Color;
import java.awt.Graphics2D;

import de.amr.easy.game.Application;
import de.amr.games.pacman.controller.actor.Ghost;
import de.amr.games.pacman.controller.actor.GhostState;
import de.amr.games.pacman.model.world.api.Population;
import de.amr.games.pacman.model.world.core.Tile;
import de.amr.games.pacman.view.render.IRenderer;

public class GhostRenderer implements IRenderer {

	private final Ghost ghost;

	public GhostRenderer(Ghost ghost) {
		this.ghost = ghost;
	}

	@Override
	public void draw(Graphics2D g) {
		if (!ghost.visible) {
			return;
		}
		smoothOn(g);
		GhostState state = ghost.getState();
		switch (state) {
		case CHASING:
		case SCATTERING:
		case LOCKED:
		case LEAVING_HOUSE:
			drawColored(g);
			break;
		case FRIGHTENED:
			if (ghost.flashing) {
				drawFlashing(g);
			} else {
				drawFrightened(g);
			}
			break;
		case DEAD:
		case ENTERING_HOUSE:
			if (ghost.points > 0) {
				drawPoints(g);
			} else {
				drawEyes(g);
			}
			break;
		default:
			break;
		}
		smoothOff(g);
	}

	private void drawEyes(Graphics2D g) {
		g.setColor(ghostColor());
		g.drawRect((int) ghost.tf.x, (int) ghost.tf.y, Tile.SIZE, Tile.SIZE);
	}

	private void drawPoints(Graphics2D g) {
		g.setColor(Color.GREEN);
		g.drawString(ghost.points + "", (int) ghost.tf.x, (int) ghost.tf.y);
	}

	private void drawFrightened(Graphics2D g) {
		g.setColor(Color.BLUE);
		g.fillRect((int) ghost.tf.x, (int) ghost.tf.y, Tile.SIZE, Tile.SIZE);
	}

	private void drawFlashing(Graphics2D g) {
		boolean flash = Application.app().clock().getTotalTicks() % 30 < 15;
		g.setColor(flash ? Color.WHITE : Color.BLUE);
		g.fillRect((int) ghost.tf.x, (int) ghost.tf.y, Tile.SIZE, Tile.SIZE);
	}

	private void drawColored(Graphics2D g) {
		g.setColor(ghostColor());
		g.fillRect((int) ghost.tf.x, (int) ghost.tf.y, Tile.SIZE, Tile.SIZE);
	}

	private Color ghostColor() {
		if (ghost.color == Population.RED_GHOST) {
			return Color.RED;
		}
		if (ghost.color == Population.PINK_GHOST) {
			return Color.PINK;
		}
		if (ghost.color == Population.CYAN_GHOST) {
			return Color.CYAN;
		}
		if (ghost.color == Population.ORANGE_GHOST) {
			return Color.ORANGE;
		}
		return null;
	}

	@Override
	public void resetAnimations() {
	}

	@Override
	public void enableAnimation(boolean enabled) {
	}
}