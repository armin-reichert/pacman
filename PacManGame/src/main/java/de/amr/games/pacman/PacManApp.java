package de.amr.games.pacman;

import java.awt.DisplayMode;

import com.beust.jcommander.Parameter;

import de.amr.easy.game.Application;
import de.amr.easy.game.config.AppSettings;
import de.amr.easy.game.ui.f2dialog.F2Dialog;
import de.amr.games.pacman.controller.EnhancedGameController;
import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.view.Localized;
import de.amr.games.pacman.view.dashboard.level.GameLevelView;
import de.amr.games.pacman.view.dashboard.states.GameStateView;
import de.amr.games.pacman.view.dashboard.theme.ThemeSelectionView;

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

		@Parameter(names = { "-simpleMode" }, description = "Strips all extra functionality not needed for just playing")
		public boolean simpleMode = false;

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
		settings.printValue("Demo Mode", "%s", settings.demoMode);
		settings.printValue("Ghosts harmless", "%s", settings.ghostsHarmless);
		settings.printValue("Ghosts flee into corner", "%s", settings.ghostsSafeCorner);
		settings.printValue("Fix Overflow Bug", "%s", settings.fixOverflowBug);
		settings.printValue("Pac-Man immortable", "%s", settings.pacManImmortable);
		settings.printValue("Pathfinder", "%s", settings.pathFinder);
		settings.printValue("Simple Mode", "%s", settings.simpleMode);
		settings.printValue("Skip Intro", "%s", settings.skipIntro);
		settings.printValue("Startlevel", "%d", settings.startLevel);
		settings.printValue("Theme", "%s", settings.theme.toUpperCase());
		settings.printValue("User Language", "%s", Localized.texts.getLocale().getDisplayLanguage());
	}

	@Override
	public void init() {
		setIcon("/images/pacman-icon.png");
		setController(settings.simpleMode ? new GameController() : new EnhancedGameController());
	}

	@Override
	public void configureF2Dialog(F2Dialog f2) {
		ThemeSelectionView themeSelectionView = new ThemeSelectionView();
		GameStateView gameStateView = new GameStateView();
		GameLevelView gameLevelView = new GameLevelView();
		GameController gameController = (GameController) getController();
		gameStateView.attachTo(gameController, gameController.folks());
		gameLevelView.attachTo(gameController);
		themeSelectionView.attachTo(gameController);
		f2.addCustomTab("Theme", themeSelectionView, () -> gameController.game().isPresent());
		f2.addCustomTab("Game State", gameStateView, () -> gameController.game().isPresent());
		f2.addCustomTab("Game Level", gameLevelView, () -> gameController.game().isPresent());
	}
}