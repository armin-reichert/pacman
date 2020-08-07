package de.amr.games.pacman.view.theme.arcade;

import java.awt.Graphics2D;

import de.amr.easy.game.entity.Entity;
import de.amr.easy.game.ui.sprites.SpriteMap;
import de.amr.games.pacman.view.api.IRenderer;

public abstract class SpriteRenderer implements IRenderer {

	protected final SpriteMap sprites = new SpriteMap();

	/**
	 * Draws the selected sprite centered over the collision box of the entity.
	 * 
	 * @param g       graphics context
	 * @param entity  entiy to be drawn
	 * @param scaling scaling of sprite size against entity size
	 */
	protected void drawEntitySprite(Graphics2D g, Entity entity, int scaling) {
		if (entity.visible) {
			sprites.current().ifPresent(sprite -> {
				Graphics2D g2 = (Graphics2D) g.create();
				int w = entity.tf.width, h = entity.tf.height;
				if (sprite.getWidth() != scaling * w || sprite.getHeight() != scaling * h) {
					g2.scale(scaling, scaling);
				}
				float x = entity.tf.x - (sprite.getWidth() - w) / 2, y = entity.tf.y - (sprite.getHeight() - h) / 2;
				sprite.draw(g2, x, y);
				g2.dispose();
			});
		}
	}
}