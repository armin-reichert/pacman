package de.amr.games.pacman.view.core;

import java.awt.Graphics2D;

import de.amr.easy.game.entity.Entity;
import de.amr.easy.game.math.Vector2f;
import de.amr.easy.game.ui.sprites.SpriteMap;

/**
 * Draws an entity using a Graphics2D context.
 * 
 * @author Armin Reichert
 */
public class EntityRenderer {

	public static void drawEntity(Graphics2D g, Entity entity, SpriteMap sprites) {
		if (entity.visible) {
			sprites.current().ifPresent(sprite -> {
				Vector2f center = entity.tf.getCenter();
				float x = center.x - sprite.getWidth() / 2;
				float y = center.y - sprite.getHeight() / 2;
				sprite.draw(g, x, y);
			});
		}
	}
}