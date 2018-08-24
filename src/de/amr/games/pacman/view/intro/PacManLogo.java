package de.amr.games.pacman.view.intro;

import java.util.stream.Stream;

import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.sprite.Sprite;

public class PacManLogo extends GameEntity {

	private Sprite sprite;

	public PacManLogo() {
		sprite = new Sprite(Assets.image("title.png"));
	}
	
	public void start() {
		init();
		tf.setVelocityY(-2f);
	}
	
	public void stop() {
		tf.setVelocityY(0);
	}

	@Override
	public void init() {
		tf.setY(288);
		hCenter(224);
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
