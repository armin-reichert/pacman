package de.amr.games.pacman.view.core;

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.util.HashMap;
import java.util.Map;

public class ThemeParameters {

	private static final Map<String, Object> parameters = new HashMap<>();
	private static final Font DEFAULT_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 8);

	public void set(String key, Object value) {
		parameters.put(key, value);
	}

	@SuppressWarnings("unchecked")
	public <T> T $value(String key) {
		return (T) parameters.get(key);
	}

	public int $int(String key) {
		return (int) parameters.getOrDefault(key, 0);
	}

	public float $float(String key) {
		return (float) parameters.getOrDefault(key, 0f);
	}

	public Color $color(String key) {
		return (Color) parameters.getOrDefault(key, Color.BLACK);
	}

	public Font $font(String key) {
		return (Font) parameters.getOrDefault(key, DEFAULT_FONT);
	}

	public Image $image(String key) {
		return (Image) parameters.get(key);
	}
}