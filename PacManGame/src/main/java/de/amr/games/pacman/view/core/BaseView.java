package de.amr.games.pacman.view.core;

import static de.amr.easy.game.Application.app;

import java.awt.Graphics2D;

import de.amr.easy.game.controller.Lifecycle;
import de.amr.easy.game.entity.Entity;
import de.amr.easy.game.math.Vector2f;
import de.amr.easy.game.ui.sprites.SpriteMap;
import de.amr.easy.game.view.View;
import de.amr.games.pacman.view.theme.Theme;

/**
 * Base class of all views in the game.
 * 
 * @author Armin Reichert
 */
public abstract class BaseView implements Lifecycle, View {

	public final Theme theme;
	public final int width, height;

	public BaseView(Theme theme) {
		this.theme = theme;
		width = app().settings().width;
		height = app().settings().height;
	}

	protected void drawActor(Graphics2D g, Entity actor, SpriteMap sprites) {
		if (actor.visible) {
			sprites.current().ifPresent(sprite -> {
				Vector2f center = actor.tf.getCenter();
				float x = center.x - sprite.getWidth() / 2;
				float y = center.y - sprite.getHeight() / 2;
				sprite.draw(g, x, y);
			});
		}
	}
}