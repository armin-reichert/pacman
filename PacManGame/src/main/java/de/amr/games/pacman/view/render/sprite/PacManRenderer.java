package de.amr.games.pacman.view.render.sprite;

import static de.amr.games.pacman.model.Direction.dirs;

import java.awt.Graphics2D;

import de.amr.games.pacman.controller.actor.PacMan;
import de.amr.games.pacman.view.render.api.IRenderer;
import de.amr.games.pacman.view.theme.Theme;

public class PacManRenderer extends CreatureRenderer implements IRenderer {

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
		switch (pacMan.getState()) {
		case DEAD:
			if (pacMan.collapsing) {
				selectSprite("dying");
			} else if (!sprites.selectedKey().equals("full")) {
				selectSprite("full");
				sprites.get("dying").resetAnimation();
			}
			drawEntity(g, pacMan);
			break;
		case RUNNING:
			selectSprite("walking-" + pacMan.moveDir());
			enableAnimation(pacMan.tf.getVelocity().length() > 0);
			drawEntity(g, pacMan);
			break;
		case SLEEPING:
			selectSprite("full");
			drawEntity(g, pacMan);
		default:
			break;
		}
	}
}