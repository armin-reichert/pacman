package de.amr.games.pacman.view.theme;

import java.util.Optional;
import java.util.stream.Stream;

import de.amr.games.pacman.view.theme.api.Theme;
import de.amr.games.pacman.view.theme.arcade.ArcadeTheme;
import de.amr.games.pacman.view.theme.blocks.BlocksTheme;
import de.amr.games.pacman.view.theme.letters.LettersTheme;

public class Themes {

	public static Stream<Theme> all() {
		return Stream.of(ArcadeTheme.IT, BlocksTheme.IT, LettersTheme.IT);
	}

	public static Optional<Theme> getThemeNamed(String name) {
		return all().filter(theme -> theme.name().equalsIgnoreCase(name)).findFirst();
	}
}