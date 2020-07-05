package de.amr.games.pacman.view.theme.arcade;

import java.awt.Graphics2D;

import de.amr.easy.game.entity.Entity;
import de.amr.easy.game.ui.sprites.SpriteMap;
import de.amr.games.pacman.model.world.core.Tile;
import de.amr.games.pacman.view.theme.IRenderer;

public abstract class CreatureRenderer implements IRenderer {

	protected final SpriteMap sprites;

	public CreatureRenderer() {
		sprites = new SpriteMap();
	}
	
	protected void drawEntity(Graphics2D g, Entity creature) {
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

	@Override
	public void enableAnimation(boolean enabled) {
		sprites.current().ifPresent(sprite -> {
			sprite.enableAnimation(enabled);
		});
	}

	@Override
	public void resetAnimations() {
		sprites.forEach(sprite -> sprite.resetAnimation());
	}
}