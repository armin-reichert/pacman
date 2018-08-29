package de.amr.games.pacman.view.core;

import java.awt.Image;

import de.amr.easy.game.entity.GameEntityUsingSprites;
import de.amr.easy.game.sprite.Sprite;

/**
 * An animation scrolling an image.
 * 
 * @author Armin Reichert
 */
public abstract class ScrollingImage extends GameEntityUsingSprites implements AnimationController {

	public ScrollingImage(Image image) {
		addSprite("s_image", new Sprite(image));
		setCurrentSprite("s_image");
		tf.setWidth(currentSprite().getWidth());
		tf.setHeight(currentSprite().getHeight());
	}

	@Override
	public void update() {
		tf.move();
	}
}