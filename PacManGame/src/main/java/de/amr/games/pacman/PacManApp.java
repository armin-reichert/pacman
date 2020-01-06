package de.amr.games.pacman;

import de.amr.easy.game.Application;
import de.amr.easy.game.config.AppSettings;
import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.theme.Theme;

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

	public static final PacManAppSettings settings = new PacManAppSettings();

	@Override
	public AppSettings createAppSettings() {
		settings.width = 28 * Tile.SIZE;
		settings.height = 36 * Tile.SIZE;
		settings.scale = 2;
		settings.title = "Armin's Pac-Man";
		return settings;
	}

	@Override
	public void init() {
		Theme theme = Theme.createTheme(settings.theme);
		GameController gameController = new GameController(theme);
		setController(gameController);
		setIcon(theme.spr_ghostFrightened().frame(0));
		setExitHandler(app -> gameController.game().ifPresent(Game::saveHiscore));
		gameController.init();
	}
}