package de.amr.games.pacman.view.theme.arcade;

import java.awt.Graphics2D;

import de.amr.games.pacman.controller.creatures.pacman.PacMan;
import de.amr.games.pacman.controller.creatures.pacman.PacManState;
import de.amr.games.pacman.model.world.api.Direction;
import de.amr.games.pacman.view.theme.api.IPacManRenderer;

public class PacManRenderer extends SpriteRenderer implements IPacManRenderer {

	private final PacMan pacMan;
	private boolean stopAnimationWhenStanding;

	public PacManRenderer(PacMan pacMan) {
		this.pacMan = pacMan;
		ArcadeThemeSprites arcadeSprites = ArcadeTheme.IT.$value("sprites");
		Direction.dirs().forEach(dir -> sprites.set("walking-" + dir, arcadeSprites.makeSprite_pacManWalking(dir)));
		sprites.set("collapsing", arcadeSprites.makeSprite_pacManDying());
		sprites.set("full", arcadeSprites.makeSprite_pacManFull());
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
		PacManState state = pacMan.getState();
		if (state == null) {
			selectSprite("full");
			return;
		}
		switch (state) {
		case DEAD:
			if (pacMan.isCollapsing()) {
				if (!"collapsing".equals(sprites.selectedKey())) {
					selectSprite("collapsing");
					sprites.get("collapsing").resetAnimation();
				}
			} else {
				selectSprite("full");
			}
			break;
		case AWAKE:
		case POWERFUL:
			selectSprite("walking-" + pacMan.moveDir());
			boolean running = pacMan.entity.tf.vx != 0 || pacMan.entity.tf.vy != 0;
			enableAnimation(running || !running && !isAnimationStoppedWhenStanding());
			break;
		case TIRED:
		case SLEEPING:
			selectSprite("full");
		default:
			break;
		}
		drawEntity(g, pacMan.entity);
	}
}