package de.amr.games.pacman.view.render.sprite;

import static de.amr.games.pacman.model.Direction.dirs;

import java.awt.Graphics2D;

import de.amr.games.pacman.controller.actor.PacMan;
import de.amr.games.pacman.view.render.api.IPacManRenderer;
import de.amr.games.pacman.view.theme.Theme;

public class PacManRenderer extends CreatureRenderer implements IPacManRenderer {

	private final PacMan pacMan;

	public PacManRenderer(PacMan pacMan, Theme theme) {
		this.pacMan = pacMan;
		dirs().forEach(dir -> sprites.set("walking-" + dir, theme.spr_pacManWalking(dir)));
		sprites.set("dying", theme.spr_pacManDying());
		sprites.set("full", theme.spr_pacManFull());
		sprites.select("full");
	}

	@Override
	public void draw(Graphics2D g) {
		drawEntity(g, pacMan);
	}

	@Override
	public void showWalking() {
		selectSprite("walking-" + pacMan.moveDir());
		enableAnimation(pacMan.tf.getVelocity().length() > 0);
	}

	@Override
	public void showDying() {
		selectSprite("dying");
	}

	@Override
	public void showFull() {
		selectSprite("full");
	}
}