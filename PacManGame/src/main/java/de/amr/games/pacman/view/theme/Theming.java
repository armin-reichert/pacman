package de.amr.games.pacman.view.theme;

import de.amr.games.pacman.view.theme.arcade.ArcadeTheme;
import de.amr.games.pacman.view.theme.blocks.BlocksTheme;

public class Theming {

	private static final Theme ARCADE_THEME = new ArcadeTheme();
	private static final Theme BLOCKS_THEME = new BlocksTheme();

	public enum ThemeName {
		ARCADE, BLOCKS;
	}

	public static Theme getTheme(ThemeName name) {
		switch (name) {
		case ARCADE:
			return ARCADE_THEME;
		case BLOCKS:
			return BLOCKS_THEME;
		default:
			throw new IllegalArgumentException("Unknown theme: " + name);
		}
	}
}