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
				int sw = 2 * pacMan.tf.width, sh = 2 * pacMan.tf.height;
				if (sw != sprite.getWidth() || sh != sprite.getHeight()) {
					sprite.scale(sw, sh);
				}
				Graphics2D g2 = (Graphics2D) g.create();
				int w = pacMan.tf.width, h = pacMan.tf.height;
				float x = pacMan.tf.x - (sprite.getWidth() - w) / 2, y = pacMan.tf.y - (sprite.getHeight() - h) / 2;
				sprite.draw(g2, x, y);
				g2.dispose();
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