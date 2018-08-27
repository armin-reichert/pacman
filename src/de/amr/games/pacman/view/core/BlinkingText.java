package de.amr.games.pacman.view.core;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Objects;
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
	private Color color;
	private int width;
	private int height;

	public BlinkingText() {
		this.text = "";
		this.font = PacManThemes.THEME.textFont();
		this.background = Color.BLACK;
		this.color = Color.YELLOW;
		createSprite();
	}
	
	public BlinkingText set(String text, Font font, Color background, Color color) {
		Objects.requireNonNull(text);
		Objects.requireNonNull(font);
		Objects.requireNonNull(background);
		Objects.requireNonNull(color);
		this.text = text;
		this.font = font;
		this.background = background;
		this.color = color;
		createSprite();
		return this;
	}
	
	public void setText(String text) {
		Objects.requireNonNull(text);
		this.text = text;
		createSprite();
	}
	
	public void setFont(Font font) {
		Objects.requireNonNull(font);
		this.font = font;
		createSprite();
	}
	
	public void setColor(Color color) {
		Objects.requireNonNull(color);
		this.color = color;
		createSprite();
	}
	
	public void setBackground(Color background) {
		Objects.requireNonNull(background);
		this.background = background;
		createSprite();
	}
	
	public void setBlinkTime(int millis) {
		sprite.animate(AnimationType.BACK_AND_FORTH, millis);
	}

	private void createSprite() {
		// compute image bounds
		BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = image.createGraphics();
		g.setFont(font);
		width = g.getFontMetrics().stringWidth(text);
		width = width == 0 ? 1 : width;
		height = font.getSize();
		g.dispose();
		// create correctly sized image
		image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		g = image.createGraphics();
		g.setColor(background);
		g.fillRect(0, 0, width, height);
		g.setColor(color);
		g.setFont(font);
		if (font.getFontName().equals("ArcadeClassic")) {
			g.drawString(text.replace(" ", "    "), 0, height);
		} else {
			g.drawString(text, 0, height);
		}
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