package de.amr.games.pacman.view.theme;

import de.amr.games.pacman.view.theme.arcade.ArcadeTheme;
import de.amr.games.pacman.view.theme.blocks.BlocksTheme;

public class Theming {

	public enum ThemeName {
		ARCADE, BLOCKS
	}

	public static Theme getTheme(ThemeName themeName) {
		switch (themeName) {
		case ARCADE:
			return new ArcadeTheme();
		case BLOCKS:
			return new BlocksTheme();
		default:
			throw new IllegalArgumentException("Unknown theme: " + themeName);
		}
	}
}