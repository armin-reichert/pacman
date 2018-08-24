package de.amr.games.pacman.view.intro;

import java.util.stream.Stream;

import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.sprite.Sprite;

public class Title extends GameEntity {

	private Sprite sprite;

	public Title() {
		sprite = new Sprite(Assets.image("title.png"));
	}

	@Override
	public void init() {
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
