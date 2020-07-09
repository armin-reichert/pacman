package de.amr.games.pacman.view.theme.arcade;

import java.awt.Graphics2D;

import de.amr.games.pacman.controller.actor.Ghost;
import de.amr.games.pacman.controller.actor.GhostState;
import de.amr.games.pacman.model.game.Game;
import de.amr.games.pacman.model.world.Direction;
import de.amr.games.pacman.view.core.IRenderer;

public class GhostRenderer extends SpriteRenderer implements IRenderer {

	private final Ghost ghost;

	public GhostRenderer(Ghost ghost) {
		this.ghost = ghost;
		ArcadeThemeAssets assets = ArcadeTheme.ASSETS;
		Direction.dirs().forEach(dir -> {
			sprites.set("color-" + dir, assets.makeSprite_ghostColored(ghost.getColor(), dir));
			sprites.set("eyes-" + dir, assets.makeSprite_ghostEyes(dir));
		});
		sprites.set("frightened", assets.makeSprite_ghostFrightened());
		sprites.set("flashing", assets.makeSprite_ghostFlashing());
		for (int points : Game.GHOST_BOUNTIES) {
			sprites.set("points-" + points, assets.makeSprite_number(points));
		}
	}

	@Override
	public void render(Graphics2D g) {
		selectSprite(ghost.getState());
		drawEntity(g, ghost);
	}

	public void selectSprite(GhostState state) {
		if (state == null) {
			selectSprite("color-" + ghost.moveDir());
		} else {
			switch (state) {
			case CHASING:
			case SCATTERING:
			case LEAVING_HOUSE:
				selectSprite("color-" + ghost.moveDir());
				break;
			case LOCKED:
				if (ghost.world().population().pacMan().power > 0) {
					selectSprite("frightened");
				} else {
					selectSprite("color-" + ghost.moveDir());
				}
				break;
			case FRIGHTENED:
				selectSprite(ghost.isFlashing() ? "flashing" : "frightened");
				break;
			case DEAD:
				selectSprite(ghost.getBounty() == 0 ? "eyes-" + ghost.moveDir() : "points-" + ghost.getBounty());
				break;
			case ENTERING_HOUSE:
				selectSprite("eyes-" + ghost.moveDir());
				break;
			default:
			}
		}
	}
}