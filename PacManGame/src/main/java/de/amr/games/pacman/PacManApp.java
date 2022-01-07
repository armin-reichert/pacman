/*
MIT License

Copyright (c) 2019 Armin Reichert

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package de.amr.games.pacman;

import java.awt.DisplayMode;

import com.beust.jcommander.Parameter;

import de.amr.easy.game.Application;
import de.amr.easy.game.config.AppSettings;
import de.amr.games.pacman.controller.game.GameController;
import de.amr.games.pacman.theme.api.Themes;
import de.amr.games.pacman.theme.arcade.ArcadeTheme;
import de.amr.games.pacman.view.api.PacManGameView;

/**
 * The Pac-Man game application.
 * 
 * @see <a href="https://github.com/armin-reichert/pacman">GitHub</a>
 * 
 * @author Armin Reichert
 */
public class PacManApp extends Application {

	static {
		Themes.registerTheme(ArcadeTheme.THEME);
	}

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
		settings.title = PacManGameView.texts.getString("app.title");
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
		settings.print("User Language", PacManGameView.texts.getLocale().getDisplayLanguage());
	}

	@Override
	public void init() {
		setIcon("/images/pacman-icon.png");
		setController(new GameController(Themes.all()));
	}
}