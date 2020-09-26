package de.amr.games.pacman.theme.arcade;

import static de.amr.games.pacman.controller.creatures.ghost.GhostState.CHASING;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.DEAD;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.ENTERING_HOUSE;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.FRIGHTENED;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.LEAVING_HOUSE;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.LOCKED;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.SCATTERING;
import static de.amr.games.pacman.theme.arcade.ArcadeTheme.THEME;

import java.awt.Graphics2D;
import java.util.Optional;

import de.amr.easy.game.ui.sprites.Sprite;
import de.amr.easy.game.ui.sprites.SpriteMap;
import de.amr.games.pacman.controller.creatures.ghost.Ghost;
import de.amr.games.pacman.controller.creatures.ghost.GhostState;
import de.amr.games.pacman.model.world.api.Direction;
import de.amr.games.pacman.theme.api.GhostRenderer;
import de.amr.games.pacman.theme.arcade.ArcadeSpritesheet.GhostColor;

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
				int spriteWidth = 2 * ghost.tf.width, spriteHeight = 2 * ghost.tf.height;
				if (spriteWidth != sprite.getWidth() || spriteHeight != sprite.getHeight()) {
					sprite.scale(spriteWidth, spriteHeight);
				}
				int width = ghost.tf.width, height = ghost.tf.height;
				float x = ghost.tf.x - (sprite.getWidth() - width) / 2, y = ghost.tf.y - (sprite.getHeight() - height) / 2;
				sprite.draw(g, x, y);
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