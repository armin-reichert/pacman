package de.amr.games.pacman;

import java.util.ResourceBundle;
import java.util.logging.Level;

import com.beust.jcommander.Parameter;

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

	public static class Settings extends AppSettings {

		@Parameter(names = { "-demoMode" }, description = "Pac-Man moves automatically, stays alive when killed")
		public boolean demoMode = false;

		@Parameter(names = { "-ghostsFleeRandomly" }, description = "default frightened ghost behavior", arity = 1)
		public boolean ghostsFleeRandomly = true;

		@Parameter(names = { "-ghostsDangerous" }, description = "if set, ghosts can kill Pac-Man", arity = 1)
		public boolean ghostsDangerous = true;

		@Parameter(names = {
				"-overflowBug" }, description = "simulate the overflow bug from the original Arcade game", arity = 1)
		public boolean overflowBug = true;

		@Parameter(names = { "-pacManImmortable" }, description = "if set, Pac-Man keeps lives when killed")
		public boolean pacManImmortable = false;

		@Parameter(names = { "-skipIntro" }, description = "start app without intro screen")
		public boolean skipIntro = false;

		@Parameter(names = { "-theme" }, description = "the theme name e.g. 'Arcade'")
		public String theme = "Arcade";
	}

	public static final ResourceBundle texts = ResourceBundle.getBundle("texts");

	public static final Settings settings = new Settings();

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