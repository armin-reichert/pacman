package de.amr.games.pacman.view.theme.arcade;

import java.awt.Graphics2D;

import de.amr.games.pacman.controller.actor.PacMan;
import de.amr.games.pacman.model.world.Direction;
import de.amr.games.pacman.view.core.IPacManRenderer;

public class PacManRenderer extends SpriteRenderer implements IPacManRenderer {

	private final PacMan pacMan;
	private boolean stopAnimationWhenStanding;

	public PacManRenderer(PacMan pacMan) {
		this.pacMan = pacMan;
		ArcadeThemeAssets assets = ArcadeTheme.ASSETS;
		Direction.dirs().forEach(dir -> sprites.set("walking-" + dir, assets.makeSprite_pacManWalking(dir)));
		sprites.set("collapsing", assets.makeSprite_pacManDying());
		sprites.set("full", assets.makeSprite_pacManFull());
		sprites.select("full");
		stopAnimationWhenStanding = true;
	}

	@Override
	public void stopAnimationWhenStanding(boolean b) {
		stopAnimationWhenStanding = b;
	}

	@Override
	public boolean isAnimationStoppedWhenStanding() {
		return stopAnimationWhenStanding;
	}

	@Override
	public void render(Graphics2D g) {
		switch (pacMan.getState()) {
		case DEAD:
			selectSprite("full");
			if (pacMan.collapsing) {
				selectSprite("collapsing");
			} else {
				sprites.get("collapsing").resetAnimation();
			}
			break;
		case RUNNING:
			selectSprite("walking-" + pacMan.moveDir());
			boolean running = pacMan.tf.vx != 0 || pacMan.tf.vy != 0;
			enableAnimation(running || !running && !isAnimationStoppedWhenStanding());
			break;
		case SLEEPING:
			selectSprite("full");
		default:
			break;
		}
		drawEntity(g, pacMan);
	}
}