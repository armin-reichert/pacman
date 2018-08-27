package de.amr.games.pacman.view.core;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;
import java.util.stream.Stream;

import de.amr.easy.game.Application;
import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.input.Mouse;
import de.amr.easy.game.sprite.Sprite;

public class Link extends GameEntity {

	private int width;
	private int height;
	private String text;
	private Font font;
	private Color color;
	private URL url;

	public Link(String text, Font font, Color color) {
		Objects.requireNonNull(text);
		this.text = text;
		this.font = font;
		this.color = color;
		computeSize();
	}

	public Link() {
		this("", new Font(Font.SANS_SERIF, Font.PLAIN, 12), Color.BLUE);
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		if (!this.text.equals(text)) {
			this.text = text;
			computeSize();
		}
	}

	public Font getFont() {
		return font;
	}

	public void setFont(Font font) {
		Objects.requireNonNull(font);
		if (!this.font.equals(font)) {
			this.font = font;
			computeSize();
		}
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		if (this.color != color) {
			this.color = color;
			computeSize();
		}
	}

	public void setURL(String spec) {
		try {
			url = new URL(spec);
		} catch (MalformedURLException e) {
			Application.LOGGER.info("Invalid link URL: " + spec);
		}
	}

	@Override
	public void init() {
	}

	@Override
	public void update() {
		if (Mouse.clicked()) {
			int x = Mouse.getX(), y = Mouse.getY();
			if (getCollisionBox().contains(new Point2D.Float(x, y))) {
				openURL();
			}
		}
	}

	@Override
	public Sprite currentSprite() {
		return null;
	}

	@Override
	public Stream<Sprite> getSprites() {
		return Stream.empty();
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
	public void draw(Graphics2D g) {
		g.translate(tf.getX(), tf.getY());
		g.setColor(color);
		g.setFont(font);
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g.drawString(text, 0, g.getFontMetrics().getAscent());
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
		g.translate(-tf.getX(), -tf.getY());
	}

	private void computeSize() {
		BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = img.createGraphics();
		g.setFont(font);
		FontMetrics fm = g.getFontMetrics();
		width = fm.stringWidth(text);
		height = fm.getHeight();
		g.dispose();
	}

	private void openURL() {
		// TODO only works under Windows OS
		try {
			Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}