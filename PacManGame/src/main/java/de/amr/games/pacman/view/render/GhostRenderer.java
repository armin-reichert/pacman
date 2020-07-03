package de.amr.games.pacman.view.render;

import java.awt.Graphics2D;

import de.amr.easy.game.ui.sprites.SpriteMap;
import de.amr.games.pacman.controller.actor.Ghost;
import de.amr.games.pacman.model.Direction;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.world.core.Tile;
import de.amr.games.pacman.view.theme.Theme;

public class GhostRenderer {

	private final Ghost ghost;
	private final SpriteMap sprites;

	public GhostRenderer(Ghost ghost, Theme theme) {
		this.ghost = ghost;
		sprites = new SpriteMap();
		Direction.dirs().forEach(dir -> {
			sprites.set("color-" + dir, theme.spr_ghostColored(ghost.color, dir));
			sprites.set("eyes-" + dir, theme.spr_ghostEyes(dir));
		});
		sprites.set("frightened", theme.spr_ghostFrightened());
		sprites.set("flashing", theme.spr_ghostFlashing());
		for (int points : Game.POINTS_GHOST) {
			sprites.set("points-" + points, theme.spr_number(points));
		}
	}

	public void selectSprite(String spriteKey) {
		sprites.select(spriteKey);
	}

	public void drawGhost(Graphics2D g) {
		if (ghost.visible) {
			sprites.current().ifPresent(sprite -> {
				float x = ghost.tf.x - Tile.SIZE / 2, y = ghost.tf.y - Tile.SIZE / 2;
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