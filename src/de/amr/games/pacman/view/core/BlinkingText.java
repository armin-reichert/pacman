package de.amr.games.pacman.view.core;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.stream.Stream;

import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.sprite.AnimationType;
import de.amr.easy.game.sprite.Sprite;
import de.amr.games.pacman.theme.PacManThemes;

/**
 * Animation showing blinking text.
 * 
 * @author Armin Reichert
 */
public class BlinkingText extends GameEntity implements ViewAnimation {

	private Sprite sprite;
	private String text;
	private Font font;
	private Color background;
	private int width;
	private int height;

	public BlinkingText(String text, float fontSize, Color background) {
		this.text = text.replace(" ", "   "); // font spacing not sufficient
		this.font = PacManThemes.THEME.textFont().deriveFont(fontSize);
		this.background = background;
		createSprite();
	}
	
	private void createSprite() {
		// compute image bounds
		BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = image.createGraphics();
		g.setFont(font);
		width = g.getFontMetrics().stringWidth(text);
		height = font.getSize();
		g.dispose();
		// create correctly sized image
		image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		g = image.createGraphics();
		g.setColor(background);
		g.fillRect(0, 0, width, height);
		g.setColor(Color.PINK);
		g.setFont(font);
		g.drawString(text, 0, height);
		g.dispose();
		sprite = new Sprite(image, null).animate(AnimationType.BACK_AND_FORTH, 750);
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}
	
	@Override
	public boolean isCompleted() {
		return false;
	}

	@Override
	public Sprite currentSprite() {
		return sprite;
	}

	@Override
	public Stream<Sprite> getSprites() {
		return Stream.of(sprite);
	}
	
	@Override
	public void draw(Graphics2D g) {
		g.setColor(background);
		g.fillRect(0, 0, getWidth(), getHeight());
		super.draw(g);
	}

	@Override
	public void init() {
	}

	@Override
	public void start() {
	}

	@Override
	public void update() {
	}
}