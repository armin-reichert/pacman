package de.amr.games.pacman;

import java.util.ResourceBundle;
import java.util.logging.Level;

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
	public static final ResourceBundle texts = ResourceBundle.getBundle("texts");

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
		LOGGER.info("User language is " + texts.getLocale().getDisplayLanguage());
		Theme theme = Theme.createTheme(settings.theme);
		setIcon(theme.spr_ghostFrightened().frame(0));
		Game.FSM_LOGGER.setLevel(Level.INFO);
		GameController gameController = new GameController(theme);
		setController(gameController);
		setExitHandler(app -> gameController.game().ifPresent(Game::saveHiscore));
	}
}