package de.amr.games.pacman.view.theme.common;

import java.awt.Font;
import java.util.HashMap;

import de.amr.games.pacman.view.api.ThemeParameters;

public class ParameterMap extends HashMap<String, Object> implements ThemeParameters {

	private static final Font DEFAULT_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 8);

	@Override
	public int $int(String key) {
		return (int) getOrDefault(key, 0);
	}

	@Override
	public float $float(String key) {
		return (float) getOrDefault(key, 0f);
	}

	@Override
	public Font $font(String key) {
		return (Font) getOrDefault(key, DEFAULT_FONT);
	}
}