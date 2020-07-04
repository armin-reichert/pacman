package de.amr.games.pacman.view.render.block;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import de.amr.games.pacman.controller.actor.PacMan;
import de.amr.games.pacman.model.world.core.Tile;
import de.amr.games.pacman.view.render.api.IPacManRenderer;
import de.amr.games.pacman.view.theme.Theme;

public class PacManRenderer implements IPacManRenderer {

	enum Mode {
		WALKING, FULL, DYING
	}

	private final PacMan pacMan;
	private Mode mode;

	public PacManRenderer(PacMan pacMan, Theme theme) {
		this.pacMan = pacMan;
		mode = Mode.FULL;
	}

	@Override
	public void enableAnimation(boolean enabled) {
	}

	@Override
	public void resetAnimations() {
	}

	@Override
	public void draw(Graphics2D g) {
		if (!pacMan.visible) {
			return;
		}
		int w = pacMan.tf.width * 2, h = pacMan.tf.height * 2;
		int x = (int) pacMan.tf.x - Tile.SIZE / 2, y = (int) pacMan.tf.y - Tile.SIZE / 2;
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(Color.YELLOW);
		if (mode == Mode.DYING) {
			g.drawOval(x, y, w, h);
		} else {
			g.fillOval(x, y, w, h);
		}
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
	}

	@Override
	public void showDying() {
		mode = Mode.DYING;
	}

	@Override
	public void showFull() {
		mode = Mode.FULL;
	}

	@Override
	public void showWalking() {
		mode = Mode.WALKING;
	}
}