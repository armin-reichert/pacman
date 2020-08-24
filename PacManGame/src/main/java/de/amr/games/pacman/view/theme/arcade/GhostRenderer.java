package de.amr.games.pacman.view.theme.arcade;

import static de.amr.games.pacman.controller.creatures.ghost.GhostState.CHASING;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.DEAD;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.ENTERING_HOUSE;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.FRIGHTENED;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.LEAVING_HOUSE;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.LOCKED;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.SCATTERING;

import java.awt.Graphics2D;
import java.util.Optional;

import de.amr.easy.game.ui.sprites.Sprite;
import de.amr.games.pacman.controller.creatures.ghost.Ghost;
import de.amr.games.pacman.controller.creatures.ghost.GhostState;
import de.amr.games.pacman.model.world.api.Direction;
import de.amr.games.pacman.view.api.IGhostRenderer;

/**
 * Renders a ghost using animated sprites.
 * 
 * @author Armin Reichert
 */
class GhostRenderer implements IGhostRenderer {

	private GhostSpriteMap spriteMap;

	public GhostRenderer(GhostSpriteMap spriteMap) {
		this.spriteMap = spriteMap;
	}

	@Override
	public void render(Graphics2D g, Ghost ghost) {
		if (ghost.body.visible) {
			selectSprite(ghost).ifPresent(sprite -> {
				sprite.enableAnimation(ghost.enabled);
				int sw = 2 * ghost.body.tf.width, sh = 2 * ghost.body.tf.height;
				if (sw != sprite.getWidth() || sh != sprite.getHeight()) {
					sprite.scale(sw, sh);
				}
				Graphics2D g2 = (Graphics2D) g.create();
				int w = ghost.body.tf.width, h = ghost.body.tf.height;
				float x = ghost.body.tf.x - (sprite.getWidth() - w) / 2, y = ghost.body.tf.y - (sprite.getHeight() - h) / 2;
				sprite.draw(g2, x, y);
				g2.dispose();
			});
		}
	}

	private Optional<Sprite> selectSprite(Ghost ghost) {
		GhostState state = ghost.ai.getState();
		Direction dir = ghost.body.moveDir;
		if (state == null) {
			return spriteMap.select(spriteMap.keyColor(ghost.personality, dir));
		} else if (ghost.ai.is(LOCKED, LEAVING_HOUSE, CHASING, SCATTERING)) {
			return spriteMap.select(spriteMap.keyColor(ghost.personality, dir));
		} else if (ghost.ai.is(ENTERING_HOUSE)) {
			return spriteMap.select(spriteMap.keyEyes(dir));
		} else if (ghost.ai.is(FRIGHTENED)) {
			return spriteMap.select(ghost.recovering ? "flashing" : "frightened");
		} else if (ghost.ai.is(DEAD)) {
			return spriteMap.select(ghost.bounty == 0 ? spriteMap.keyEyes(dir) : spriteMap.keyPoints(ghost.bounty));
		}
		return Optional.empty();
	}
}