package de.amr.games.pacman;

import java.util.ResourceBundle;
import java.util.logging.Level;

import de.amr.easy.game.Application;
import de.amr.easy.game.config.AppSettings;
import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.controller.PacManStateMachineLogging;
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

	public static final ResourceBundle texts = ResourceBundle.getBundle("texts");

	public static final PacManAppSettings settings = new PacManAppSettings();

	public static void main(String[] args) {
		launch(PacManApp.class, settings, args);
	}

	@Override
	protected void configure(AppSettings settings) {
		settings.width = 28 * Tile.SIZE;
		settings.height = 36 * Tile.SIZE;
		settings.scale = 2;
		settings.title = texts.getString("app.title");
	}

	@Override
	protected void printSettings() {
		super.printSettings();
		loginfo("\tGhosts dangerous: %s", settings.ghostsDangerous);
		loginfo("\tGhosts flee randomly: %s", settings.ghostsFleeRandomly);
		loginfo("\tOverflow Bug: %s", settings.overflowBug);
		loginfo("\tPacMan immortable: %s", settings.pacManImmortable);
		loginfo("\tSkip Intro: %s", settings.skipIntro);
		loginfo("\tTheme: %s", settings.theme);

	}

	@Override
	public void init() {
		loginfo("User language is %s", texts.getLocale().getDisplayLanguage());
		Theme theme = Theme.createTheme(settings.theme);
		setIcon(theme.spr_ghostFrightened().frame(0));
		PacManStateMachineLogging.setLevel(Level.INFO);
		GameController gameController = new GameController(theme);
		setExitHandler(app -> gameController.onExit());
		setController(gameController);
	}
}