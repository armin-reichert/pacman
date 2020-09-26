package de.amr.games.pacman.theme.api;

import java.util.List;
import java.util.Optional;

import de.amr.games.pacman.theme.arcade.ArcadeTheme;
import de.amr.games.pacman.theme.blocks.BlocksTheme;
import de.amr.games.pacman.theme.letters.LettersTheme;

/**
 * The predefined themes.
 * 
 * @author Armin Reichert
 */
public class Themes {

	public static List<Theme> all() {
		return List.of(ArcadeTheme.THEME, BlocksTheme.THEME, LettersTheme.THEME);
	}

	public static Optional<Theme> getTheme(String name) {
		return all().stream().filter(theme -> theme.name().equalsIgnoreCase(name)).findFirst();
	}
}