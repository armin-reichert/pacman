package de.amr.games.pacman.view.theme.blocks;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import de.amr.easy.game.Application;
import de.amr.games.pacman.controller.creatures.ghost.Ghost;
import de.amr.games.pacman.controller.creatures.ghost.GhostState;
import de.amr.games.pacman.view.api.IGhostRenderer;
import de.amr.games.pacman.view.common.Rendering;

class GhostRenderer implements IGhostRenderer {

	@Override
	public void render(Graphics2D g, Ghost ghost) {
		if (!ghost.entity.visible) {
			return;
		}
		GhostState state = ghost.ai.getState();
		if (state == null) {
			state = GhostState.CHASING;
		}
		Rendering.smoothOn(g);
		int width = 2 * ghost.entity.tf.width - 4, height = 2 * ghost.entity.tf.height - 2;
		switch (state) {
		case CHASING:
		case SCATTERING:
		case LOCKED:
		case LEAVING_HOUSE:
			drawColored(g, ghost, width, height, 0, 0);
			break;
		case FRIGHTENED:
			if (ghost.isFlashing()) {
				drawFlashing(g, ghost, width, height, 0, 0);
			} else {
				drawFrightened(g, ghost, width, height, 0, 0);
			}
			break;
		case DEAD:
		case ENTERING_HOUSE:
			if (ghost.getBounty() > 0) {
				drawPoints(g, ghost);
			} else {
				drawEyes(g, ghost, ghost.entity.tf.width, ghost.entity.tf.width);
			}
			break;
		default:
			break;
		}
		Rendering.smoothOff(g);
	}

	private void drawEyes(Graphics2D g, Ghost ghost, int width, int height) {
		int x = centerOffsetX(ghost, width), y = centerOffsetY(ghost, height);
		g.setColor(BlocksTheme.THEME.ghostColor(ghost));
		g.drawRect(x, y, width, height);
	}

	private void drawPoints(Graphics2D g, Ghost ghost) {
		g.setColor(Color.GREEN);
		Font font = BlocksTheme.THEME.$font("font").deriveFont((float) ghost.entity.tf.height);
		g.setFont(font);
		FontMetrics fm = g.getFontMetrics();
		String text = String.valueOf(ghost.getBounty());
		Rectangle2D bounds = fm.getStringBounds(text, g);
		int x = centerOffsetX(ghost, (int) bounds.getWidth());
		int y = centerOffsetY(ghost, (int) bounds.getHeight()) + (int) bounds.getHeight();
		g.drawString(text, x, y);
	}

	private void drawFrightened(Graphics2D g, Ghost ghost, int width, int height, int offsetX, int offsetY) {
		drawShape(g, ghost, width, height, offsetX, offsetY, Color.BLUE);
	}

	private void drawFlashing(Graphics2D g, Ghost ghost, int width, int height, int offsetX, int offsetY) {
		boolean flash = Application.app().clock().getTotalTicks() % 30 < 15;
		drawShape(g, ghost, width, height, offsetX, offsetY, flash ? Color.WHITE : Color.BLUE);
	}

	private void drawColored(Graphics2D g, Ghost ghost, int width, int height, int offsetX, int offsetY) {
		drawShape(g, ghost, width, height, offsetX, offsetY, BlocksTheme.THEME.ghostColor(ghost));
	}

	private void drawShape(Graphics2D g, Ghost ghost, int width, int height, int offsetX, int offsetY, Color color) {
		int x = centerOffsetX(ghost, width) + offsetX, y = centerOffsetY(ghost, height) + offsetY;
		g.setColor(color);
		g.fillRect(x, y, width, height);
		g.fillArc(x, y - height / 4 - 2, width, height, 0, 180);
		g.setColor(Color.BLACK);
		g.fillRect(x, y, width, 2);
		g.setColor(Color.WHITE);
		g.fillRect(x, y + height - 2, width, 2);
	}

	private int centerOffsetX(Ghost ghost, int width) {
		return (int) ghost.entity.tf.x + (ghost.entity.tf.width - width) / 2;
	}

	private int centerOffsetY(Ghost ghost, int height) {
		return (int) ghost.entity.tf.y + (ghost.entity.tf.height - height) / 2;
	}
}