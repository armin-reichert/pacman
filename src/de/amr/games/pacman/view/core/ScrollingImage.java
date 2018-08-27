package de.amr.games.pacman.view.core;

import java.awt.Image;
import java.util.stream.Stream;

import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.sprite.Sprite;

/**
 * An animation scrolling an image.
 * 
 * @author Armin Reichert
 */
public abstract class ScrollingImage extends GameEntity implements ViewAnimation {

	private Sprite sprite;

	public ScrollingImage(Image image) {
		sprite = new Sprite(image);
	}

	@Override
	public void update() {
		tf.move();
	}

	@Override
	public Sprite currentSprite() {
		return sprite;
	}

	@Override
	public Stream<Sprite> getSprites() {
		return Stream.of(sprite);
	}
}