package de.amr.games.pacman.view.theme.blocks;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

import de.amr.easy.game.Application;
import de.amr.games.pacman.controller.creatures.ghost.Ghost;
import de.amr.games.pacman.controller.creatures.ghost.GhostState;
import de.amr.games.pacman.model.world.api.Tile;
import de.amr.games.pacman.view.theme.api.IRenderer;

class GhostRenderer implements IRenderer {

	private final Ghost ghost;

	public GhostRenderer(Ghost ghost) {
		this.ghost = ghost;
	}

	@Override
	public void render(Graphics2D g) {
		if (!ghost.isVisible()) {
			return;
		}
		GhostState state = ghost.getState();
		if (state == null) {
			smoothDrawingOn(g);
			drawColored(g);
			smoothDrawingOff(g);
			return;
		}
		smoothDrawingOn(g);
		switch (state) {
		case CHASING:
		case SCATTERING:
		case LOCKED:
		case LEAVING_HOUSE:
			drawColored(g);
			break;
		case FRIGHTENED:
			if (ghost.isFlashing()) {
				drawFlashing(g);
			} else {
				drawFrightened(g);
			}
			break;
		case DEAD:
		case ENTERING_HOUSE:
			if (ghost.getBounty() > 0) {
				drawPoints(g);
			} else {
				drawEyes(g);
			}
			break;
		default:
			break;
		}
		smoothDrawingOff(g);
	}

	private int tiles(double amount) {
		return (int) (amount * Tile.SIZE);
	}

	private void drawEyes(Graphics2D g) {
		int size = tiles(1);
		int x = (int) ghost.entity.tf.x + (ghost.entity.tf.width - size) / 2;
		int y = (int) ghost.entity.tf.y + (ghost.entity.tf.width - size) / 2;
		g.setColor(BlocksTheme.THEME.ghostColor(ghost));
		g.drawRect(x, y, size, size);
	}

	private void drawPoints(Graphics2D g) {
		g.setColor(Color.GREEN);
		g.setFont(BlocksTheme.THEME.$font("font"));
		FontMetrics fm = g.getFontMetrics();
		g.drawString(ghost.getBounty() + "", (int) ghost.entity.tf.x - 4, (int) ghost.entity.tf.y + fm.getAscent());
	}

	private void drawFrightened(Graphics2D g) {
		int size = tiles(1.5);
		int x = (int) ghost.entity.tf.x + (ghost.entity.tf.width - size) / 2;
		int y = (int) ghost.entity.tf.y + (ghost.entity.tf.width - size) / 2;
		g.translate(0, 2);
		g.setColor(Color.BLUE);
		g.fillRect(x, y, size, size);
		g.fillArc(x, y - Tile.SIZE / 2, size, size, 0, 180);
		g.translate(0, -2);
	}

	private void drawFlashing(Graphics2D g) {
		boolean flash = Application.app().clock().getTotalTicks() % 30 < 15;
		int size = tiles(1.5);
		int x = (int) ghost.entity.tf.x + (ghost.entity.tf.width - size) / 2;
		int y = (int) ghost.entity.tf.y + (ghost.entity.tf.width - size) / 2;
		g.translate(0, 2);
		g.setColor(flash ? Color.WHITE : Color.BLUE);
		g.fillRect(x, y, size, size);
		g.fillArc(x, y - Tile.SIZE / 2, size, size, 0, 180);
		g.translate(0, -2);
	}

	private void drawColored(Graphics2D g) {
		int size = tiles(1.5);
		int x = (int) ghost.entity.tf.x + (ghost.entity.tf.width - size) / 2;
		int y = (int) ghost.entity.tf.y + (ghost.entity.tf.width - size) / 2;
		g.translate(0, 2);
		g.setColor(BlocksTheme.THEME.ghostColor(ghost));
		g.fillRect(x, y, size, size);
		g.fillArc(x, y - Tile.SIZE / 2, size, size, 0, 180);
		g.translate(0, -2);
	}
}