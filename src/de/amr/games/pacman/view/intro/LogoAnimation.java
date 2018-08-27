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
	private final int stopY;
	private Sprite sprite;

	public LogoAnimation(int panelWidth, int panelHeight, int stopY) {
		this.panelWidth = panelWidth;
		this.panelHeight = panelHeight;
		this.stopY = stopY;
		sprite = new Sprite(Assets.image("logo.png"));
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

	public void start() {
		init();
		tf.setVelocityY(-2f);
	}

	public void stop() {
		tf.setVelocityY(0);
	}

	public boolean isCompleted() {
		return tf.getY() <= stopY;
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