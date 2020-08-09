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
import de.amr.games.pacman.model.world.api.Direction;
import de.amr.games.pacman.view.api.IPacManRenderer;

/**
 * Renders Pac-Man using animated sprites.
 * 
 * @author Armin Reichert
 */
public class PacManRenderer extends SpriteRenderer implements IPacManRenderer {

	public PacManRenderer() {
		ArcadeSprites sprites = ArcadeTheme.THEME.$value("sprites");
		Direction.dirs().forEach(dir -> spriteMap.set("walking-" + dir, sprites.makeSprite_pacManWalking(dir)));
		spriteMap.set("collapsing", sprites.makeSprite_pacManCollapsing());
		spriteMap.set("full", sprites.makeSprite_pacManFull());
	}

	@Override
	public void render(Graphics2D g, PacMan pacMan) {
		selectSprite(pacMan).ifPresent(sprite -> {
			sprite.enableAnimation(pacMan.isEnabled());
			drawEntitySprite(g, pacMan.entity, 2);
		});
	}

	private Optional<Sprite> selectSprite(PacMan pacMan) {
		if (pacMan.getState() == null || pacMan.is(IN_BED, SLEEPING)) {
			return spriteMap.select("full");
		} else if (pacMan.is(AWAKE, POWERFUL)) {
			return spriteMap.select("walking-" + pacMan.moveDir());
		} else if (pacMan.is(DEAD)) {
			spriteMap.get("collapsing").resetAnimation();
			return spriteMap.select("full");
		} else if (pacMan.is(PacManState.COLLAPSING)) {
			return spriteMap.select("collapsing");
		}
		throw new IllegalStateException();
	}
}