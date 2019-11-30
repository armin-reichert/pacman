package de.amr.games.pacman;

import static de.amr.games.pacman.model.PacManGame.TS;

import java.util.logging.Level;
import java.util.logging.Logger;

import de.amr.easy.game.Application;
import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.theme.ClassicPacManTheme;
import de.amr.games.pacman.theme.PacManTheme;

/**
 * The Pac-Man game application.
 * 
 * @see <a href="https://github.com/armin-reichert/pacman">GitHub</a>
 * 
 * @author Armin Reichert
 */
public class PacManApp extends Application {

	public static void main(String[] args) {
		launch(new PacManApp(), args);
	}

	public PacManApp() {
		settings.title = "Armin's Pac-Man";
		settings.width = Maze.COLS * TS;
		settings.height = Maze.ROWS * TS;
		settings.scale = 2;
		settings.fullScreenOnStart = false;
		settings.set("overflowBug", true);
		settings.set("skipIntro", true);
		settings.set("ghost.originalBehavior", true);
		settings.set("pacMan.immortable", false);
	}

	@Override
	public void init() {
		PacManTheme theme = new ClassicPacManTheme();
		PacManGame game = new PacManGame(theme);
		PacManGameController gameController = new PacManGameController(game);
		setController(gameController);
		setIcon(theme.spr_ghostFrightened().frame(0));
		Logger.getLogger("StateMachineLogger").setLevel(Level.OFF);
	}
}