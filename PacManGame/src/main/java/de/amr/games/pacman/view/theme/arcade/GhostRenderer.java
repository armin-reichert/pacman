package de.amr.games.pacman.view.theme.arcade;

import java.awt.Graphics2D;

import de.amr.easy.game.ui.sprites.Sprite;
import de.amr.games.pacman.controller.creatures.ghost.Ghost;
import de.amr.games.pacman.controller.creatures.ghost.GhostPersonality;
import de.amr.games.pacman.controller.creatures.ghost.GhostState;
import de.amr.games.pacman.model.world.api.Direction;
import de.amr.games.pacman.view.api.IGhostRenderer;
import de.amr.games.pacman.view.common.ISpriteRenderer;

/**
 * Renders a ghost using animated sprites.
 * 
 * @author Armin Reichert
 */
class GhostRenderer implements IGhostRenderer, ISpriteRenderer {

	private GhostSpriteMap spriteMap;

	public GhostRenderer(GhostSpriteMap spriteMap) {
		this.spriteMap = spriteMap;
	}

	@Override
	public void render(Graphics2D g, Ghost ghost) {
		selectSprite(ghost);
		Sprite sprite = spriteMap.current().get();
		sprite.enableAnimation(ghost.isEnabled());
		drawEntitySprite(g, ghost.entity, sprite, 2);
	}

	private void selectSprite(Ghost ghost) {
		GhostState state = ghost.getState();
		GhostPersonality personality = ghost.getPersonality();
		Direction dir = ghost.moveDir();
		if (state == null) {
			spriteMap.select(GhostSpriteMap.keyColor(personality, dir));
		} else {
			switch (state) {
			case LOCKED:
			case LEAVING_HOUSE:
			case CHASING:
			case SCATTERING:
				spriteMap.select(GhostSpriteMap.keyColor(personality, dir));
				break;
			case ENTERING_HOUSE:
				spriteMap.select(GhostSpriteMap.keyEyes(dir));
				break;
			case FRIGHTENED:
				spriteMap.select(ghost.isFlashing() ? "flashing" : "frightened");
				break;
			case DEAD:
				spriteMap
						.select(ghost.getBounty() == 0 ? GhostSpriteMap.keyEyes(dir) : GhostSpriteMap.keyPoints(ghost.getBounty()));
				break;
			default:
				break;
			}
		}
	}
}