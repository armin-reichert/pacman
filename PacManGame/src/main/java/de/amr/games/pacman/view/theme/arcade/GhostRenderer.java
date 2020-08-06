package de.amr.games.pacman.view.theme.arcade;

import java.awt.Graphics2D;

import de.amr.games.pacman.controller.creatures.ghost.Ghost;
import de.amr.games.pacman.controller.creatures.ghost.GhostState;
import de.amr.games.pacman.model.game.Game;
import de.amr.games.pacman.model.world.api.Direction;
import de.amr.games.pacman.view.theme.api.IGhostRenderer;

public class GhostRenderer extends SpriteRenderer implements IGhostRenderer {

	public GhostRenderer(Ghost ghost) {
		ArcadeThemeSprites arcadeSprites = ArcadeTheme.THEME.$value("sprites");
		Direction.dirs().forEach(dir -> {
			sprites.set("color-" + dir, arcadeSprites.makeSprite_ghostColored(ghost.getPersonality(), dir));
			sprites.set("eyes-" + dir, arcadeSprites.makeSprite_ghostEyes(dir));
		});
		sprites.set("frightened", arcadeSprites.makeSprite_ghostFrightened());
		sprites.set("flashing", arcadeSprites.makeSprite_ghostFlashing());
		for (int points : Game.GHOST_BOUNTIES) {
			sprites.set("points-" + points, arcadeSprites.makeSprite_number(points));
		}
	}

	@Override
	public void render(Graphics2D g, Ghost ghost) {
		selectSprite(ghost);
		drawEntity(g, ghost.entity, 2);
	}

	public void selectSprite(Ghost ghost) {
		GhostState state = ghost.getState();
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
				selectSprite("color-" + ghost.moveDir());
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