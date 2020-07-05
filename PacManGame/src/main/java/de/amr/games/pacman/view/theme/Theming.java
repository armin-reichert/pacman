package de.amr.games.pacman.view.theme;

import de.amr.games.pacman.model.world.api.World;
import de.amr.games.pacman.view.theme.arcade.ArcadeTheme;
import de.amr.games.pacman.view.theme.blocks.BlocksTheme;

public class Theming {

	public enum ThemeName {
		ARCADE, BLOCKS
	}

	public static IWorldRenderer createWorldRenderer(ThemeName theme, World world) {
		if (theme == ThemeName.ARCADE) {
			return new de.amr.games.pacman.view.theme.arcade.WorldRenderer(world);
		} else {
			return new de.amr.games.pacman.view.theme.blocks.WorldRenderer(world);
		}
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