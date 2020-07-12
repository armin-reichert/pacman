package de.amr.games.pacman.view.theme;

import java.util.Optional;
import java.util.stream.Stream;

import de.amr.games.pacman.view.theme.api.Theme;
import de.amr.games.pacman.view.theme.arcade.ArcadeTheme;
import de.amr.games.pacman.view.theme.blocks.BlocksTheme;
import de.amr.games.pacman.view.theme.letters.LettersTheme;

public class Themes {

	public static final Theme ARCADE_THEME = new ArcadeTheme();
	public static final Theme BLOCKS_THEME = new BlocksTheme();
	public static final Theme LETTERS_THEME = new LettersTheme();

	public static Stream<Theme> all() {
		return Stream.of(ARCADE_THEME, BLOCKS_THEME, LETTERS_THEME);
	}

	public static Optional<Theme> getThemeNamed(String name) {
		return all().filter(theme -> theme.name().equalsIgnoreCase(name)).findFirst();
	}
}