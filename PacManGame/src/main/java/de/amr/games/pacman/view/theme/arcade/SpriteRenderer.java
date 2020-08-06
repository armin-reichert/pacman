package de.amr.games.pacman.view.theme.arcade;

import java.awt.Graphics2D;

import de.amr.easy.game.entity.Entity;
import de.amr.easy.game.ui.sprites.SpriteMap;
import de.amr.games.pacman.view.theme.api.IRenderer;

public abstract class SpriteRenderer implements IRenderer {

	protected final SpriteMap sprites;

	public SpriteRenderer() {
		sprites = new SpriteMap();
	}

	protected void drawEntity(Graphics2D g, Entity entity, int scaling) {
		if (entity.visible) {
			sprites.current().ifPresent(sprite -> {
				int w = entity.tf.width, h = entity.tf.height;
				if (sprite.getWidth() != scaling * w || sprite.getHeight() != scaling * h) {
					sprite = sprite.scale(scaling * w, scaling * h);
				}
				float x = entity.tf.x - (sprite.getWidth() - w) / 2, y = entity.tf.y - (sprite.getHeight() - h) / 2;
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