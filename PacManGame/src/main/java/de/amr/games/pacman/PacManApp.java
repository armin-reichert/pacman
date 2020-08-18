
package de.amr.games.pacman;

import java.awt.DisplayMode;

import com.beust.jcommander.Parameter;

import de.amr.easy.game.Application;
import de.amr.easy.game.config.AppSettings;
import de.amr.games.pacman.controller.game.GameController;
import de.amr.games.pacman.view.Localized;
import de.amr.games.pacman.view.api.Theme;
import de.amr.games.pacman.view.theme.Themes;

/**
 * The Pac-Man game application.
 * 
 * @see <a href="https://github.com/armin-reichert/pacman">GitHub</a>
 * 
 * @author Armin Reichert
 */
public class PacManApp extends Application {

	public static void main(String[] args) {
		launch(PacManApp.class, settings, args);
	}

	// Application configuration

	public static class Settings extends AppSettings {

		@Parameter(names = { "-demoMode" }, description = "Pac-Man moves automatically")
		public boolean demoMode = false;

		@Parameter(names = { "-ghostsHarmless" }, description = "Ghost collisions are harmless")
		public boolean ghostsHarmless = false;

		@Parameter(names = { "-ghostsSafeCorner" }, description = "Ghosts don't flee randomly but to a safe corner")
		public boolean ghostsSafeCorner = false;

		@Parameter(names = { "-fixOverflowBug" }, description = "Fixes the overflow bug from the original Arcade game")
		public boolean fixOverflowBug = false;

		@Parameter(names = { "-pacManImmortable" }, description = "Pac-Man stays alive when killed by ghost")
		public boolean pacManImmortable = false;

		@Parameter(names = { "-pathFinder" }, description = "Used path finding algorithm (astar, bfs, bestfs)")
		public String pathFinder = "astar";

		@Parameter(names = { "-skipIntro" }, description = "Game starts without intro screen")
		public boolean skipIntro = false;

		@Parameter(names = { "-startLevel" }, description = "Game starts in specified level")
		public int startLevel = 1;

		@Parameter(names = { "-theme" }, description = "Used Theme (arcade, blocks, letters)")
		public String theme = "arcade";
	}

	public static final Settings settings = new Settings();

	@Override
	protected void configure(AppSettings settings) {
		settings.width = 224;
		settings.height = 288;
		settings.scale = 2;
		settings.title = Localized.texts.getString("app.title");
		settings.fullScreenMode = new DisplayMode(800, 600, 32, 50);
	}

	@Override
	protected void printSettings() {
		super.printSettings();
		settings.print("Demo Mode", settings.demoMode);
		settings.print("Ghosts harmless", settings.ghostsHarmless);
		settings.print("Ghosts flee into corner", settings.ghostsSafeCorner);
		settings.print("Fix Overflow Bug", settings.fixOverflowBug);
		settings.print("Pac-Man immortable", settings.pacManImmortable);
		settings.print("Pathfinder", settings.pathFinder);
		settings.print("Skip Intro", settings.skipIntro);
		settings.print("Startlevel", settings.startLevel);
		settings.print("Theme", settings.theme.toUpperCase());
		settings.print("User Language", Localized.texts.getLocale().getDisplayLanguage());
	}

	@Override
	public void init() {
		setIcon("/images/pacman-icon.png");
		setController(new GameController(Themes.all().toArray(Theme[]::new)));
	}
}