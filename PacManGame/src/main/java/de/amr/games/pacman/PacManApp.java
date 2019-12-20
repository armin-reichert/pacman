package de.amr.games.pacman;

import de.amr.easy.game.Application;
import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.model.Tile;
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
		settings.width = 28 * Tile.SIZE;
		settings.height = 36 * Tile.SIZE;
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