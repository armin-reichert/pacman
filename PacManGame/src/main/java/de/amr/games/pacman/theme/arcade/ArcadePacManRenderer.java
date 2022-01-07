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

import static de.amr.games.pacman.controller.creatures.pacman.PacManState.AWAKE;
import static de.amr.games.pacman.controller.creatures.pacman.PacManState.DEAD;
import static de.amr.games.pacman.controller.creatures.pacman.PacManState.IN_BED;
import static de.amr.games.pacman.controller.creatures.pacman.PacManState.POWERFUL;
import static de.amr.games.pacman.controller.creatures.pacman.PacManState.SLEEPING;

import java.awt.Graphics2D;
import java.util.Optional;

import de.amr.easy.game.ui.sprites.Sprite;
import de.amr.easy.game.ui.sprites.SpriteMap;
import de.amr.games.pacman.controller.creatures.pacman.PacMan;
import de.amr.games.pacman.controller.creatures.pacman.PacManState;
import de.amr.games.pacman.theme.api.PacManRenderer;

/**
 * Renders Pac-Man using animated sprites.
 * 
 * @author Armin Reichert
 */
class ArcadePacManRenderer implements PacManRenderer {

	@Override
	public void resetAnimations(PacMan pacMan) {
		SpriteMap spriteMap = ArcadeTheme.THEME.getSpriteMap(pacMan);
		spriteMap.forEach(sprite -> sprite.resetAnimation());
	}

	@Override
	public void render(Graphics2D g, PacMan pacMan) {
		if (pacMan.visible) {
			selectSprite(pacMan).ifPresent(sprite -> {
				int spriteWidth = 2 * pacMan.tf.width, spriteHeight = 2 * pacMan.tf.height;
				if (spriteWidth != sprite.getWidth() || spriteHeight != sprite.getHeight()) {
					sprite.scale(spriteWidth, spriteHeight);
				}
				int width = pacMan.tf.width, height = pacMan.tf.height;
				float x = pacMan.tf.x - (sprite.getWidth() - width) / 2, y = pacMan.tf.y - (sprite.getHeight() - height) / 2;
				sprite.draw(g, x, y);
			});
		}
	}

	private Optional<Sprite> selectSprite(PacMan pacMan) {
		SpriteMap spriteMap = ArcadeTheme.THEME.getSpriteMap(pacMan);
		if (pacMan.ai.getState() == null || pacMan.ai.is(IN_BED, SLEEPING)) {
			return spriteMap.select("full");
		} else if (pacMan.ai.is(AWAKE, POWERFUL)) {
			boolean blocked = !pacMan.canMoveTo(pacMan.moveDir);
			return spriteMap.select((blocked ? "blocked-" : "walking-") + pacMan.moveDir);
		} else if (pacMan.ai.is(DEAD)) {
			return spriteMap.select("full");
		} else if (pacMan.ai.is(PacManState.COLLAPSING)) {
			return spriteMap.select("collapsing");
		}
		throw new IllegalStateException();
	}
}