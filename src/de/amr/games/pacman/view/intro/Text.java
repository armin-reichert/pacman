package de.amr.games.pacman.view.intro;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.stream.Stream;

import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.sprite.AnimationType;
import de.amr.easy.game.sprite.Sprite;
import de.amr.games.pacman.theme.PacManTheme;

public class Text extends GameEntity {

	private Sprite sprite;

	public Text(String text, float fontSize) {
		Font font = PacManTheme.ASSETS.textFont().deriveFont(fontSize);
		BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = image.createGraphics();
		g.setFont(font);
		int w = g.getFontMetrics().stringWidth(text);
		g.dispose();
		image = new BufferedImage(w, 16, BufferedImage.TYPE_INT_RGB);
		g = image.createGraphics();
		g.setColor(Color.YELLOW);
		g.setFont(font);
		g.drawString(text, 0, 16);
		g.dispose();
		sprite = new Sprite(image, null).animate(AnimationType.BACK_AND_FORTH, 1000);
	}

	@Override
	public void init() {
	}

	@Override
	public void update() {
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