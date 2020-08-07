package de.amr.games.pacman.view.theme.arcade;

import java.awt.Graphics2D;

import de.amr.games.pacman.controller.creatures.pacman.PacMan;
import de.amr.games.pacman.controller.creatures.pacman.PacManState;
import de.amr.games.pacman.model.world.api.Direction;
import de.amr.games.pacman.view.theme.api.IPacManRenderer;

public class PacManRenderer extends SpriteRenderer implements IPacManRenderer {

	public PacManRenderer() {
		ArcadeThemeSprites arcadeSprites = ArcadeTheme.THEME.$value("sprites");
		Direction.dirs().forEach(dir -> sprites.set("walking-" + dir, arcadeSprites.makeSprite_pacManWalking(dir)));
		sprites.set("collapsing", arcadeSprites.makeSprite_pacManDying());
		sprites.set("full", arcadeSprites.makeSprite_pacManFull());
		sprites.select("full");
	}

	@Override
	public void render(Graphics2D g, PacMan pacMan) {
		PacManState state = pacMan.getState();
		if (state == null) {
			selectSprite("full");
			return;
		}
		switch (state) {
		case AWAKE:
		case POWERFUL:
			selectSprite("walking-" + pacMan.moveDir());
			sprites.current().get().enableAnimation(pacMan.isEnabled() || pacMan.alwaysEnabled);
			break;
		case IN_BED:
		case SLEEPING:
			selectSprite("full");
		case DEAD:
			selectSprite("full");
			break;
		case COLLAPSING:
			if (!"collapsing".equals(sprites.selectedKey())) {
				sprites.get("collapsing").resetAnimation();
				selectSprite("collapsing");
			}
			break;
		default:
			break;
		}
		drawEntity(g, pacMan.entity, 2);
	}
}