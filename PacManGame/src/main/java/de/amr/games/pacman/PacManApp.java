package de.amr.games.pacman;

import java.util.logging.Level;
import java.util.logging.Logger;

import de.amr.easy.game.Application;
import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.model.Maze;
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

	static {
		Logger.getLogger("StateMachineLogger").setLevel(Level.OFF);
	}

	public static void main(String[] args) {
		launch(new PacManApp(), args);
	}

	public PacManApp() {
		settings.width = Maze.NUM_COLS * Maze.TS;
		settings.height = Maze.NUM_ROWS * Maze.TS;
		settings.scale = 2;
		settings.title = "Armin's Pac-Man";
		settings.set("overflowBug", true);
		settings.set("skipIntro", false);
		settings.set("ghost.originalBehavior", true);
		settings.set("pacMan.immortable", false);
	}

	@Override
	public void init() {
		PacManTheme theme = new ClassicPacManTheme();
		PacManGameController gameController = new PacManGameController(theme);
		setController(gameController);
		setIcon(theme.spr_ghostFrightened().frame(0));
	}
}