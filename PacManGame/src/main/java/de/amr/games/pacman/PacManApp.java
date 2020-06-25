package de.amr.games.pacman;

import java.awt.DisplayMode;
import java.util.ResourceBundle;

import com.beust.jcommander.Parameter;

import de.amr.easy.game.Application;
import de.amr.easy.game.config.AppSettings;
import de.amr.easy.game.ui.f2dialog.F2DialogAPI;
import de.amr.games.pacman.controller.EnhancedGameController;
import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.controller.PacManStateMachineLogging;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.view.dashboard.level.GameLevelView;
import de.amr.games.pacman.view.dashboard.states.GameStateView;
import de.amr.games.pacman.view.theme.ArcadeTheme;
import de.amr.games.pacman.view.theme.Theme;

/**
 * The Pac-Man game application.
 * 
 * @see <a href="https://github.com/armin-reichert/pacman">GitHub</a>
 * 
 * @author Armin Reichert
 */
public class PacManApp extends Application {

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
	}

	public static final ResourceBundle texts = ResourceBundle.getBundle("texts");

	public static final Settings settings = new Settings();

	public static void main(String[] args) {
		launch(PacManApp.class, settings, args);
	}

	private GameController controller;

	@Override
	protected void configure(AppSettings settings) {
		settings.width = 28 * Tile.SIZE;
		settings.height = 36 * Tile.SIZE;
		settings.scale = 2;
		settings.title = texts.getString("app.title");
		settings.fullScreenMode = new DisplayMode(800, 600, 32, 60);
		PacManStateMachineLogging.setEnabled(false);
	}

	@Override
	protected void printSettings() {
		super.printSettings();
		printValue("Demo Mode", "%s", settings.demoMode);
		printValue("Ghosts harmless", "%s", settings.ghostsHarmless);
		printValue("Ghosts flee into corner", "%s", settings.ghostsSafeCorner);
		printValue("Fix Overflow Bug", "%s", settings.fixOverflowBug);
		printValue("Pac-Man immortable", "%s", settings.pacManImmortable);
		printValue("Pathfinder", "%s", settings.pathFinder);
		printValue("Simple Mode", "%s", settings.simpleMode);
		printValue("Skip Intro", "%s", settings.skipIntro);
		printValue("Startlevel", "%d", settings.startLevel);
		printValue("User Language", "%s", texts.getLocale().getDisplayLanguage());
	}

	@Override
	public void init() {
		Theme theme = new ArcadeTheme();
		controller = settings.simpleMode ? new GameController(theme) : new EnhancedGameController(theme);
		setController(controller);
		setIcon(theme.spr_ghostFrightened().frame(0));
		onEntry(ApplicationState.CLOSED, state -> controller.saveScore());
	}

	@Override
	public void configureF2Dialog(F2DialogAPI dialog) {
		GameStateView gameStateView = new GameStateView();
		dialog.addCustomTab("Game State", gameStateView, () -> controller.game != null);
		gameStateView.attachTo(controller);
		GameLevelView gameLevelView = new GameLevelView();
		dialog.addCustomTab("Game Level", gameLevelView, () -> controller.game != null);
		gameLevelView.attachTo(controller);
		dialog.selectCustomTab(0);
	}

}