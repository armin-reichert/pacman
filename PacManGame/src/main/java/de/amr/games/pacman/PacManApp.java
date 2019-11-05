package de.amr.games.pacman;

import java.util.logging.Level;
import java.util.logging.Logger;

import de.amr.easy.game.Application;
import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.theme.ClassicPacManTheme;

/**
 * The Pac-Man game application.
 * 
 * @see <a href="https://github.com/armin-reichert/pacman">GitHub</a>
 * 
 * @author Armin Reichert
 */
public class PacManApp extends Application {

	public static void main(String[] args) {
		Logger.getLogger("StateMachineLogger").setLevel(Level.OFF);
		launch(new PacManApp(), args);
	}

	public PacManApp() {
		settings.title = "Armin's Pac-Man";
		settings.width = 28 * PacManGame.TS;
		settings.height = 36 * PacManGame.TS;
		settings.scale = 2;
		settings.fullScreenOnStart = false;
	}

	@Override
	public void init() {
		PacManGame game = new PacManGame(new ClassicPacManTheme());
		PacManGameController gameController = new PacManGameController(game);
		setController(gameController);
		setIcon(game.theme.spr_ghostFrightened().frame(0));
	}
}