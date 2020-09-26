package de.amr.games.pacman.theme.api;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * The predefined themes.
 * 
 * @author Armin Reichert
 */
public class Themes {

	private static Set<Theme> REGISTERED_THEMES = new HashSet<>();

	public static List<Theme> all() {
		return new ArrayList<>(REGISTERED_THEMES);
	}

	public static void registerTheme(Theme theme) {
		REGISTERED_THEMES.add(theme);
	}

	public static Optional<Theme> getTheme(String name) {
		return all().stream().filter(theme -> theme.name().equalsIgnoreCase(name)).findFirst();
	}
}