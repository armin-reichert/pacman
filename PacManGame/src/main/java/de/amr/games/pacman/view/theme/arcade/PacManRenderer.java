package de.amr.games.pacman.view.theme.arcade;

import static de.amr.games.pacman.controller.creatures.pacman.PacManState.AWAKE;
import static de.amr.games.pacman.controller.creatures.pacman.PacManState.DEAD;
import static de.amr.games.pacman.controller.creatures.pacman.PacManState.IN_BED;
import static de.amr.games.pacman.controller.creatures.pacman.PacManState.POWERFUL;
import static de.amr.games.pacman.controller.creatures.pacman.PacManState.SLEEPING;

import java.awt.Graphics2D;
import java.util.Optional;

import de.amr.easy.game.ui.sprites.Sprite;
import de.amr.games.pacman.controller.creatures.pacman.PacMan;
import de.amr.games.pacman.controller.creatures.pacman.PacManState;
import de.amr.games.pacman.view.api.IPacManRenderer;
import de.amr.games.pacman.view.theme.PacManSpriteMap;

/**
 * Renders Pac-Man using animated sprites.
 * 
 * @author Armin Reichert
 */
class PacManRenderer implements IPacManRenderer {

	private PacManSpriteMap spriteMap;

	public PacManRenderer(PacManSpriteMap spriteMap) {
		this.spriteMap = spriteMap;
	}

	@Override
	public void resetAnimations() {
		spriteMap.forEach(sprite -> sprite.resetAnimation());
	}

	@Override
	public void render(Graphics2D g, PacMan pacMan) {
		if (pacMan.body.visible) {
			selectSprite(pacMan).ifPresent(sprite -> {
				sprite.enableAnimation(pacMan.enabled);
				int sw = 2 * pacMan.body.tf.width, sh = 2 * pacMan.body.tf.height;
				if (sw != sprite.getWidth() || sh != sprite.getHeight()) {
					sprite.scale(sw, sh);
				}
				Graphics2D g2 = (Graphics2D) g.create();
				int w = pacMan.body.tf.width, h = pacMan.body.tf.height;
				float x = pacMan.body.tf.x - (sprite.getWidth() - w) / 2, y = pacMan.body.tf.y - (sprite.getHeight() - h) / 2;
				sprite.draw(g2, x, y);
				g2.dispose();
			});
		}
	}

	private Optional<Sprite> selectSprite(PacMan pacMan) {
		if (pacMan.ai.getState() == null || pacMan.ai.is(IN_BED, SLEEPING)) {
			return spriteMap.select("full");
		} else if (pacMan.ai.is(AWAKE, POWERFUL)) {
			return spriteMap.select("walking-" + pacMan.body.moveDir);
		} else if (pacMan.ai.is(DEAD)) {
			return spriteMap.select("full");
		} else if (pacMan.ai.is(PacManState.COLLAPSING)) {
			return spriteMap.select("collapsing");
		}
		throw new IllegalStateException();
	}
}