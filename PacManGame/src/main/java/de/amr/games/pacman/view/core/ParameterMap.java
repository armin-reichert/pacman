package de.amr.games.pacman.view.core;

import java.awt.Color;
import java.awt.Font;
import java.util.HashMap;

import de.amr.games.pacman.view.api.ThemeParameters;

public class ParameterMap extends HashMap<String, Object> implements ThemeParameters {

	private static final Font DEFAULT_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 8);

	@SuppressWarnings("unchecked")
	@Override
	public <T> T $value(String key) {
		return (T) get(key);
	}

	@Override
	public int $int(String key) {
		return (int) getOrDefault(key, 0);
	}

	@Override
	public float $float(String key) {
		return (float) getOrDefault(key, 0f);
	}

	@Override
	public Color $color(String key) {
		return (Color) getOrDefault(key, Color.BLACK);
	}

	@Override
	public Font $font(String key) {
		return (Font) getOrDefault(key, DEFAULT_FONT);
	}
}