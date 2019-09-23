package de.amr.games.pacman;

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

	public static void main(String[] args) {
		LOGGER.setLevel(Level.INFO);
		launch(new PacManApp(), args);
	}

	public PacManApp() {
		PacManTheme theme = new ClassicPacManTheme();
		setIcon(theme.spr_ghostFrightened().frame(0));
		settings.set("theme", theme);
		settings.width = 28 * PacManGame.TS;
		settings.height = 36 * PacManGame.TS;
		settings.scale = 2;
		settings.title = "Armin's Pac-Man";
		settings.fullScreenOnStart = false;
	}

	@Override
	public void init() {
		// clock.setLoggingEnabled(true);
		setController(new PacManGameController());
	}
}