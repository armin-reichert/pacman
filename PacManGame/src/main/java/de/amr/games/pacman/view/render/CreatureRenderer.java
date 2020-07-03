package de.amr.games.pacman.view.render;

import java.awt.Graphics2D;

import de.amr.easy.game.ui.sprites.SpriteMap;
import de.amr.games.pacman.controller.actor.Creature;
import de.amr.games.pacman.model.world.core.Tile;

public abstract class CreatureRenderer {

	protected final SpriteMap sprites;

	public CreatureRenderer() {
		sprites = new SpriteMap();
	}

	public abstract void draw(Graphics2D g);

	protected void draw(Graphics2D g, Creature<?> creature) {
		if (creature.visible) {
			sprites.current().ifPresent(sprite -> {
				float x = creature.tf.x - Tile.SIZE / 2, y = creature.tf.y - Tile.SIZE / 2;
				sprite.draw(g, x, y);
			});
		}
	}

	public void selectSprite(String spriteKey) {
		sprites.select(spriteKey);
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