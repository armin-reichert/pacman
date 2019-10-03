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
		launch(new PacManApp(new ClassicPacManTheme()), args);
	}

	protected final PacManTheme theme;

	public PacManApp(PacManTheme theme) {
		this.theme = theme;
		setIcon(theme.spr_ghostFrightened().frame(0));
		// Default application settings, can be overwritten by command-line arguments
		settings.title = "Armin's Pac-Man";
		settings.width = 28 * PacManGame.TS;
		settings.height = 36 * PacManGame.TS;
		settings.scale = 2;
		settings.fullScreenOnStart = false;
	}

	@Override
	public void init() {
		PacManGame game = new PacManGame(theme);
		PacManGameController gameController = new PacManGameController(game);
		gameController.traceTo(LOGGER, app().clock::getFrequency);
		setController(gameController);
	}
}