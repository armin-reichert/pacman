package de.amr.games.pacman.view.theme.arcade;

import static de.amr.games.pacman.controller.creatures.ghost.GhostState.CHASING;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.DEAD;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.ENTERING_HOUSE;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.FRIGHTENED;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.LEAVING_HOUSE;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.LOCKED;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.SCATTERING;

import java.awt.Graphics2D;

import de.amr.easy.game.ui.sprites.Sprite;
import de.amr.games.pacman.controller.creatures.ghost.Ghost;
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
		sprite.enableAnimation(ghost.enabled);
		int sw = 2 * ghost.entity.tf.width, sh = 2 * ghost.entity.tf.height;
		if (sw != sprite.getWidth() || sh != sprite.getHeight()) {
			sprite.scale(sw, sh);
		}
		drawEntitySprite(g, ghost.entity, sprite);
	}

	private void selectSprite(Ghost ghost) {
		GhostState state = ghost.ai.getState();
		Direction dir = ghost.entity.moveDir;
		if (state == null) {
			spriteMap.select(spriteMap.keyColor(ghost.personality, dir));
		} else if (ghost.ai.is(LOCKED, LEAVING_HOUSE, CHASING, SCATTERING)) {
			spriteMap.select(spriteMap.keyColor(ghost.personality, dir));
		} else if (ghost.ai.is(ENTERING_HOUSE)) {
			spriteMap.select(spriteMap.keyEyes(dir));
		} else if (ghost.ai.is(FRIGHTENED)) {
			spriteMap.select(ghost.flashing ? "flashing" : "frightened");
		} else if (ghost.ai.is(DEAD)) {
			spriteMap.select(ghost.bounty == 0 ? spriteMap.keyEyes(dir) : spriteMap.keyPoints(ghost.bounty));
		}
	}
}