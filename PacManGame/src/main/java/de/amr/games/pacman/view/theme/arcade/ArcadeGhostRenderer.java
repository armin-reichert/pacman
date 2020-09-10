package de.amr.games.pacman.view.theme.arcade;

import static de.amr.games.pacman.controller.creatures.ghost.GhostState.CHASING;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.DEAD;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.ENTERING_HOUSE;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.FRIGHTENED;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.LEAVING_HOUSE;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.LOCKED;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.SCATTERING;
import static de.amr.games.pacman.view.theme.arcade.ArcadeTheme.THEME;

import java.awt.Graphics2D;
import java.util.Optional;

import de.amr.easy.game.ui.sprites.Sprite;
import de.amr.easy.game.ui.sprites.SpriteMap;
import de.amr.games.pacman.controller.creatures.ghost.Ghost;
import de.amr.games.pacman.controller.creatures.ghost.GhostState;
import de.amr.games.pacman.model.world.api.Direction;
import de.amr.games.pacman.view.api.GhostRenderer;
import de.amr.games.pacman.view.theme.arcade.ArcadeSpritesheet.GhostColor;

/**
 * Renders a ghost using animated sprites.
 * 
 * @author Armin Reichert
 */
class ArcadeGhostRenderer implements GhostRenderer {

	@Override
	public void render(Graphics2D g, Ghost ghost) {
		if (ghost.visible) {
			selectSprite(ghost).ifPresent(sprite -> {
				int sw = 2 * ghost.tf.width, sh = 2 * ghost.tf.height;
				if (sw != sprite.getWidth() || sh != sprite.getHeight()) {
					sprite.scale(sw, sh);
				}
				Graphics2D g2 = (Graphics2D) g.create();
				int w = ghost.tf.width, h = ghost.tf.height;
				float x = ghost.tf.x - (sprite.getWidth() - w) / 2, y = ghost.tf.y - (sprite.getHeight() - h) / 2;
				sprite.draw(g2, x, y);
				g2.dispose();
			});
		}
	}

	private Optional<Sprite> selectSprite(Ghost ghost) {
		SpriteMap spriteMap = THEME.getSpriteMap(ghost);
		GhostState state = ghost.ai.getState();
		Direction dir = ghost.moveDir;
		GhostColor color = THEME.color(ghost.personality);
		if (state == null) {
			return spriteMap.select(THEME.ghostSpriteKeyColor(color, dir));
		} else if (ghost.ai.is(LOCKED, LEAVING_HOUSE, CHASING, SCATTERING)) {
			return spriteMap.select(THEME.ghostSpriteKeyColor(color, dir));
		} else if (ghost.ai.is(ENTERING_HOUSE)) {
			return spriteMap.select(THEME.ghostSpriteKeyEyes(dir));
		} else if (ghost.ai.is(FRIGHTENED)) {
			return spriteMap.select(ghost.recovering ? "flashing" : "frightened");
		} else if (ghost.ai.is(DEAD)) {
			return spriteMap
					.select(ghost.bounty == 0 ? THEME.ghostSpriteKeyEyes(dir) : THEME.ghostSpriteKeyPoints(ghost.bounty));
		}
		return Optional.empty();
	}
}