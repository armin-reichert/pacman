package de.amr.games.pacman.theme.core;

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.util.HashMap;
import java.util.Map;

public class ThemeParameterMap {

	protected final Map<String, Object> parameters = new HashMap<>();

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
		return (Font) parameters.get(key);
	}

	public Image $image(String key) {
		return (Image) parameters.get(key);
	}
}