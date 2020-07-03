package de.amr.games.pacman.view.render;

import static de.amr.games.pacman.model.Direction.dirs;

import java.awt.Graphics2D;

import de.amr.easy.game.ui.sprites.SpriteMap;
import de.amr.games.pacman.controller.actor.PacMan;
import de.amr.games.pacman.model.world.core.Tile;
import de.amr.games.pacman.view.theme.Theme;

public class PacManRenderer {

	private final PacMan pacMan;
	private final SpriteMap sprites;

	public PacManRenderer(PacMan pacMan, Theme theme) {
		this.pacMan = pacMan;
		sprites = new SpriteMap();
		dirs().forEach(dir -> sprites.set("walking-" + dir, theme.spr_pacManWalking(dir)));
		sprites.set("dying", theme.spr_pacManDying());
		sprites.set("full", theme.spr_pacManFull());
	}

	public void selectSprite(String spriteKey) {
		sprites.select(spriteKey);
	}

	public void drawPacMan(Graphics2D g) {
		if (pacMan.visible) {
			sprites.current().ifPresent(sprite -> {
				float x = pacMan.tf.x - Tile.SIZE / 2, y = pacMan.tf.y - Tile.SIZE / 2;
				sprite.draw(g, x, y);
			});
		}
	}

	public void enableSpriteAnimation(boolean enabled) {
		sprites.current().ifPresent(sprite -> {
			sprite.enableAnimation(enabled);
		});
	}

	public void resetAnimations() {
		sprites.forEach(sprite -> sprite.resetAnimation());
	}
}