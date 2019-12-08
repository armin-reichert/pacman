package de.amr.games.pacman;

import java.util.logging.Level;
import java.util.logging.Logger;

import de.amr.easy.game.Application;
import de.amr.easy.game.input.Keyboard;
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
		Logger.getLogger("StateMachineLogger").setLevel(Level.OFF);
		Logger.getLogger(Keyboard.class.getName()).setLevel(Level.OFF);
		launch(new PacManApp(), args);
	}

	public PacManApp() {
		settings.title = "Armin's Pac-Man";
		settings.width = Maze.COLS * Maze.TS;
		settings.height = Maze.ROWS * Maze.TS;
		settings.scale = 2;
		settings.fullScreenOnStart = false;
		settings.set("overflowBug", true);
		settings.set("skipIntro", false);
		settings.set("ghost.originalBehavior", true);
		settings.set("pacMan.immortable", false);
	}

	@Override
	public void init() {
		PacManTheme theme = new ClassicPacManTheme();
		PacManGame game = new PacManGame();
		PacManGameController gameController = new PacManGameController(game, theme);
		setController(gameController);
		setIcon(theme.spr_ghostFrightened().frame(0));
	}
}