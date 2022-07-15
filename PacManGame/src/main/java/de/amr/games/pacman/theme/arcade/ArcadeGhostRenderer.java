/*
MIT License

Copyright (c) 2019 Armin Reichert

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
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
import de.amr.games.pacman.lib.Direction;
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
				int spriteWidth = 2 * ghost.tf.width;
				int spriteHeight = 2 * ghost.tf.height;
				if (spriteWidth != sprite.getWidth() || spriteHeight != sprite.getHeight()) {
					sprite.scale(spriteWidth, spriteHeight);
				}
				int width = ghost.tf.width;
				int height = ghost.tf.height;
				float x = ghost.tf.x - (sprite.getWidth() - width) / 2;
				float y = ghost.tf.y - (sprite.getHeight() - height) / 2;
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