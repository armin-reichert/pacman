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
			state = GhostState.CHASING;
		}
		smoothDrawingOn(g);
		int width = 2 * ghost.entity.tf.width - 2, height = 2 * ghost.entity.tf.height;
		switch (state) {
		case CHASING:
		case SCATTERING:
		case LOCKED:
		case LEAVING_HOUSE:
			drawColored(g, width, height, 0, 0);
			break;
		case FRIGHTENED:
			if (ghost.isFlashing()) {
				drawFlashing(g, width, height, 0, 0);
			} else {
				drawFrightened(g, width, height, 0, 0);
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

	private void drawEyes(Graphics2D g) {
		int width = Tile.SIZE, height = Tile.SIZE;
		int x = centeredX(width), y = centeredY(height);
		g.setColor(BlocksTheme.THEME.ghostColor(ghost));
		g.drawRect(x, y, width, height);
	}

	private void drawPoints(Graphics2D g) {
		g.setColor(Color.GREEN);
		g.setFont(BlocksTheme.THEME.$font("font"));
		FontMetrics fm = g.getFontMetrics();
		g.drawString(ghost.getBounty() + "", (int) ghost.entity.tf.x - 4, (int) ghost.entity.tf.y + fm.getAscent());
	}

	private void drawFrightened(Graphics2D g, int width, int height, int offsetX, int offsetY) {
		drawShape(g, width, height, offsetX, offsetY, Color.BLUE);
	}

	private void drawFlashing(Graphics2D g, int width, int height, int offsetX, int offsetY) {
		boolean flash = Application.app().clock().getTotalTicks() % 30 < 15;
		drawShape(g, width, height, offsetX, offsetY, flash ? Color.WHITE : Color.BLUE);
	}

	private void drawColored(Graphics2D g, int width, int height, int offsetX, int offsetY) {
		drawShape(g, width, height, offsetX, offsetY, BlocksTheme.THEME.ghostColor(ghost));
	}

	private void drawShape(Graphics2D g, int width, int height, int offsetX, int offsetY, Color color) {
		int x = centeredX(width) + offsetX, y = centeredY(height) + offsetY;
		g.setColor(color);
		g.fillRect(x, y, width, height);
		g.fillArc(x, y - height / 4 - 2, width, height, 0, 180);
		g.setColor(Color.BLACK);
		g.fillRect(x, y, width, 1);
		g.setColor(Color.WHITE);
		g.fillRect(x, y + height - 1, width, 1);
	}

	private int centeredX(int width) {
		return (int) ghost.entity.tf.x + (ghost.entity.tf.width - width) / 2;
	}

	private int centeredY(int height) {
		return (int) ghost.entity.tf.y + (ghost.entity.tf.height - height) / 2;
	}
}