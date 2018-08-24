package de.amr.games.pacman.view.intro;

import java.util.stream.Stream;

import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.sprite.Sprite;

/**
 * An animation scrolling the Pac-Man logo into the visible area.
 * 
 * @author Armin Reichert
 */
public class LogoAnimation extends GameEntity {

	private final int panelWidth;
	private final int panelHeight;
	private final int stopPosition;
	private Sprite sprite;

	public LogoAnimation(int width, int height, int stopPosition) {
		this.panelWidth = width;
		this.panelHeight = height;
		this.stopPosition = stopPosition;
		sprite = new Sprite(Assets.image("logo.png"));
	}
	
	public void start() {
		init();
		tf.setVelocityY(-2f);
	}
	
	public void stop() {
		tf.setVelocityY(0);
	}
	
	public boolean isCompleted() {
		return tf.getY() <= stopPosition;
	}

	@Override
	public void init() {
		tf.setY(panelHeight);
		hCenter(panelWidth);
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