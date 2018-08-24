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

public class BlinkingStartText extends GameEntity {

	private final Sprite sprite;
	private final int width;
	private final float fontSize;

	public BlinkingStartText(String text, float fontSize) {
		this.fontSize = fontSize;
		Font font = PacManTheme.ASSETS.textFont().deriveFont(fontSize);
		// compute image bounds
		BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = image.createGraphics();
		g.setFont(font);
		width = g.getFontMetrics().stringWidth(text);
		g.dispose();
		// create correctly sized image
		image = new BufferedImage(width, (int) fontSize, BufferedImage.TYPE_INT_RGB);
		g = image.createGraphics();
		g.setColor(Color.PINK);
		g.setFont(font);
		g.drawString(text, 0, 16);
		g.dispose();
		sprite = new Sprite(image, null).animate(AnimationType.BACK_AND_FORTH, 750);
	}
	
//	@Override
//	public void draw(Graphics2D g) {
//		super.draw(g);
//		g.translate(tf.getX(), tf.getY());
//		g.setColor(Color.WHITE);
//		g.drawRect(0, 0, getWidth(), getHeight());
//		g.translate(-tf.getX(), -tf.getY());
//	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return (int) fontSize;
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