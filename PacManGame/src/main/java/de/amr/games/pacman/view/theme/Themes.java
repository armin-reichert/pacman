package de.amr.games.pacman.view.theme;

import java.util.Optional;
import java.util.stream.Stream;

import de.amr.games.pacman.view.theme.arcade.ArcadeTheme;
import de.amr.games.pacman.view.theme.ascii.AsciiTheme;
import de.amr.games.pacman.view.theme.blocks.BlocksTheme;

public class Themes {

	public static final Theme ARCADE_THEME = new ArcadeTheme();
	public static final Theme BLOCKS_THEME = new BlocksTheme();
	public static final Theme ASCII_THEME = new AsciiTheme();

	public static Optional<Theme> byName(String name) {
		return Stream.of(ARCADE_THEME, BLOCKS_THEME, ASCII_THEME).filter(theme -> theme.name().equalsIgnoreCase(name))
				.findFirst();
	}
}