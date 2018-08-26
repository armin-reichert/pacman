package de.amr.games.pacman.theme;

import de.amr.easy.game.Application;

public class PacManThemes {

	public static PacManTheme THEME;

	public static void use(Class<? extends PacManTheme> themeClass) {
		try {
			THEME = themeClass.newInstance();
			Application.LOGGER.info("Pac-Man theme created.");
		} catch (Exception e) {
			e.printStackTrace();
			Application.LOGGER.info(String.format("Could not create Pac-Man theme for class '%s",
					themeClass.getSimpleName()));
		}
	}
}