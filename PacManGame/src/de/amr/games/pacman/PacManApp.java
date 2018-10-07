package de.amr.games.pacman;

import java.awt.DisplayMode;
import java.util.logging.Level;

import de.amr.easy.game.Application;
import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.theme.ClassicPacManTheme;
import de.amr.games.pacman.theme.PacManTheme;

/**
 * Pac-Man game.
 * 
 * @see <a href="https://github.com/armin-reichert/pacman">GitHub</a>
 * 
 * @author Armin Reichert
 */
public class PacManApp extends Application {

	public static PacManTheme theme;

	public static void main(String[] args) {
		LOGGER.setLevel(Level.INFO);
		try {
			theme = new ClassicPacManTheme();
			LOGGER.info(String.format("Theme '%s' created.", theme.getClass().getSimpleName()));
			launch(new PacManApp(), args);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public PacManApp() {
		settings.width = 28 * PacManGame.TS;
		settings.height = 36 * PacManGame.TS;
		settings.scale = 2;
		settings.title = "Armin's Pac-Man";
		settings.fullScreenMode = new DisplayMode(800, 600, 32, DisplayMode.REFRESH_RATE_UNKNOWN);
		settings.fullScreenOnStart = false;
		setIcon(theme.spr_ghostFrightened().frame(0));
	}

	@Override
	public void init() {
		setController(new PacManGameController());
	}
}