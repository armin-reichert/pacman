package de.amr.games.pacman;

import com.beust.jcommander.Parameter;

import de.amr.easy.game.Application;
import de.amr.easy.game.config.AppSettings;
import de.amr.games.pacman.PacManApp.PacManAppSettings;
import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.theme.Theme;
import de.amr.games.pacman.theme.Themes;

/**
 * The Pac-Man game application.
 * 
 * @see <a href="https://github.com/armin-reichert/pacman">GitHub</a>
 * 
 * @author Armin Reichert
 */
public class PacManApp extends Application<PacManAppSettings> {

	public class PacManAppSettings extends AppSettings {

		@Parameter(names = { "-skipIntro" }, description = "start app without intro screen")
		public boolean skipIntro = false;

		@Parameter(names = { "-overflowBug" }, description = "simulate the overflow bug from the original Arcade game")
		public boolean overflowBug = true;

		@Parameter(names = { "-ghostsFleeRandomly" }, description = "default ghost behavior when FRIGHTENED")
		public boolean ghostsFleeRandomly = true;

		@Parameter(names = { "-pacManImmortable" }, description = "if set, Pac-Man keeps lives when killed")
		public boolean pacManImmortable = false;

		@Parameter(names = { "-theme" }, description = "the theme name e.g. 'Arcade'")
		public String theme = "Arcade";
	}

	public static void main(String[] args) {
		launch(new PacManApp(), args);
	}

	@Override
	public PacManAppSettings createAppSettings() {
		PacManAppSettings settings = new PacManAppSettings();
		settings.width = 28 * Tile.SIZE;
		settings.height = 36 * Tile.SIZE;
		settings.scale = 2;
		settings.title = "Armin's Pac-Man";
		return settings;
	}

	@Override
	public void init() {
		Theme theme = Themes.createTheme(settings.theme);
		GameController gameController = new GameController(theme);
		exitHandler = app -> gameController.game().ifPresent(Game::saveHiscore);
		setController(gameController);
		setIcon(theme.spr_ghostFrightened().frame(0));
		gameController.init();
	}
}