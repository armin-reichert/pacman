package de.amr.games.pacman.theme.api;

import java.util.Optional;
import java.util.stream.Stream;

import de.amr.games.pacman.theme.arcade.ArcadeTheme;
import de.amr.games.pacman.theme.blocks.BlocksTheme;
import de.amr.games.pacman.theme.letters.LettersTheme;

/**
 * The predefined themes.
 * 
 * @author Armin Reichert
 */
public class Themes {

	public static Stream<Theme> all() {
		return Stream.of(ArcadeTheme.THEME, BlocksTheme.THEME, LettersTheme.THEME);
	}

	public static Optional<Theme> getTheme(String name) {
		return all().filter(theme -> theme.name().equalsIgnoreCase(name)).findFirst();
	}
}