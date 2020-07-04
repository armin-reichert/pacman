package de.amr.games.pacman.view.render.block;

import static de.amr.easy.game.Application.app;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import de.amr.games.pacman.controller.actor.Ghost;
import de.amr.games.pacman.model.world.core.Tile;
import de.amr.games.pacman.view.render.api.IGhostRenderer;
import de.amr.games.pacman.view.theme.Theme;

public class GhostRenderer implements IGhostRenderer {

	enum Mode {
		COLORED, FRIGHTENED, BLINKING, EYES, POINTS;

		int points;
	}

	private final Ghost ghost;
	private Mode mode;

	public GhostRenderer(Ghost ghost, Theme theme) {
		this.ghost = ghost;
		mode = Mode.COLORED;
	}

	@Override
	public void draw(Graphics2D g) {
		if (!ghost.visible) {
			return;
		}
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		int w = ghost.tf.width * 2, h = ghost.tf.height * 2;
		int x = (int) ghost.tf.x - Tile.SIZE / 2, y = (int) ghost.tf.y - Tile.SIZE / 2;
		Color color = blockBackground(mode);
		if (color != null) {
			g.setColor(color);
			if (mode != Mode.EYES) {
				g.fillRect(x, y, w, h);
			} else {
				g.drawRect(x, y, w, h);
			}
			if (mode == Mode.FRIGHTENED || mode == Mode.BLINKING || mode == Mode.EYES) {
				g.setColor(ghostColor());
				g.fillOval(x + Tile.SIZE / 2, y + Tile.SIZE / 2, Tile.SIZE, Tile.SIZE);
			}
		}
		String text = text(mode);
		if (text != null) {
			g.setColor(Color.GREEN);
			g.drawString(text, (int) ghost.tf.x, (int) ghost.tf.y);
		}
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
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

	private Color blockBackground(Mode mode) {
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

	private String text(Mode mode) {
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
		mode = Mode.COLORED;
	}

	@Override
	public void showFrightened() {
		mode = Mode.FRIGHTENED;
	}

	@Override
	public void showEyes() {
		mode = Mode.EYES;
	}

	@Override
	public void showFlashing() {
		mode = Mode.BLINKING;
	}

	@Override
	public void showPoints(int points) {
		mode = Mode.POINTS;
		mode.points = points;
	}
}