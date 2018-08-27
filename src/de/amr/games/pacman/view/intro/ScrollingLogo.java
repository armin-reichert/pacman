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
	public void init() {
		hCenter(parentWidth);
		tf.setY(parentHeight);
	}
	
	@Override
	public void start() {
		init();
		tf.setVelocityY(-2f);
	}
	
	@Override
	public boolean isCompleted() {
		return tf.getY() <= 20;
	}
}