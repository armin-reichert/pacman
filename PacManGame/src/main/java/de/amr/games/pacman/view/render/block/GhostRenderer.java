package de.amr.games.pacman.view.render.block;

import static de.amr.easy.game.Application.app;

import java.awt.Color;
import java.awt.Graphics2D;

import de.amr.games.pacman.controller.actor.Ghost;
import de.amr.games.pacman.model.world.core.Tile;
import de.amr.games.pacman.view.render.api.IGhostRenderer;
import de.amr.games.pacman.view.theme.Theme;

public class GhostRenderer implements IGhostRenderer {

	enum GhostMode {
		COLORED, FRIGHTENED, BLINKING, EYES, POINTS;

		int points;
	}

	private final Ghost ghost;
	private GhostMode mode;

	public GhostRenderer(Ghost ghost, Theme theme) {
		this.ghost = ghost;
		mode = GhostMode.COLORED;
	}

	@Override
	public void draw(Graphics2D g) {
		if (!ghost.visible) {
			return;
		}
		smoothOn(g);
		int w = ghost.tf.width * 2, h = ghost.tf.height * 2;
		int x = (int) ghost.tf.x - Tile.SIZE / 2, y = (int) ghost.tf.y - Tile.SIZE / 2;
		Color color = blockBackground(mode);
		if (color != null) {
			g.setColor(color);
			if (mode != GhostMode.EYES) {
				g.fillRect(x, y, w, h);
			} else {
				g.drawRect(x, y, w, h);
			}
			if (mode == GhostMode.FRIGHTENED || mode == GhostMode.BLINKING || mode == GhostMode.EYES) {
				g.setColor(ghostColor());
				g.fillOval(x + Tile.SIZE / 2, y + Tile.SIZE / 2, Tile.SIZE, Tile.SIZE);
			}
		}
		String text = text(mode);
		if (text != null) {
			g.setColor(Color.GREEN);
			g.drawString(text, (int) ghost.tf.x, (int) ghost.tf.y);
		}
		smoothOff(g);
	}

	private Color ghostColor() {
		if (ghost.color == Theme.RED_GHOST) {
			return Color.RED;
		}
		if (ghost.color == Theme.PINK_GHOST) {
			return Color.PINK;
		}
		if (ghost.color == Theme.CYAN_GHOST) {
			return Color.CYAN;
		}
		if (ghost.color == Theme.ORANGE_GHOST) {
			return Color.ORANGE;
		}
		return null;
	}

	private Color blockBackground(GhostMode mode) {
		switch (mode) {
		case COLORED:
			return ghostColor();
		case FRIGHTENED:
			return Color.BLUE;
		case BLINKING:
			return app().clock().getTotalTicks() % 30 < 15 ? Color.BLUE : Color.WHITE;
		case EYES:
			return Color.WHITE;
		case POINTS:
		default:
			return null;
		}
	}

	private String text(GhostMode mode) {
		switch (mode) {
		case COLORED:
		case FRIGHTENED:
		case BLINKING:
		case EYES:
			return null;
		case POINTS:
			return mode.points + "";
		default:
			return null;
		}
	}

	@Override
	public void resetAnimations() {
	}

	@Override
	public void enableAnimation(boolean enabled) {
	}

	@Override
	public void showColored() {
		mode = GhostMode.COLORED;
	}

	@Override
	public void showFrightened() {
		mode = GhostMode.FRIGHTENED;
	}

	@Override
	public void showEyes() {
		mode = GhostMode.EYES;
	}

	@Override
	public void showFlashing() {
		mode = GhostMode.BLINKING;
	}

	@Override
	public void showPoints(int points) {
		mode = GhostMode.POINTS;
		mode.points = points;
	}
}