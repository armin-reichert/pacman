package de.amr.games.pacman.view.intro;

import de.amr.easy.game.assets.Assets;
import de.amr.games.pacman.view.core.ScrollingImage;

public class ScrollingLogo extends ScrollingImage {

	private int parentWidth;
	private int parentHeight;

	public ScrollingLogo(int parentWidth, int parentHeight) {
		super(Assets.image("logo.png"));
		this.parentWidth = parentWidth;
		this.parentHeight = parentHeight;
	}
	
	@Override
	public int getWidth() {
		return currentSprite().getWidth();
	}
	
	@Override
	public int getHeight() {
		return currentSprite().getHeight();
	}

	@Override
	public void init() {
		centerHorizontally(parentWidth);
		tf.setY(parentHeight);
	}

	@Override
	public void startAnimation() {
		init();
		tf.setVelocityY(-2f);
	}

	@Override
	public void stopAnimation() {
		tf.setVelocityY(0);
	}

	@Override
	public boolean isAnimationCompleted() {
		return tf.getY() <= 20;
	}
}