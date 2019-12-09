package de.amr.games.pacman.test.navigation;

import de.amr.easy.game.Application;
import de.amr.games.pacman.actor.PacManGameCast;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.theme.ClassicPacManTheme;
import de.amr.games.pacman.theme.PacManTheme;

public class TakeShortestPathTestApp extends Application {

	public static void main(String[] args) {
		launch(new TakeShortestPathTestApp(), args);
	}

	public TakeShortestPathTestApp() {
		settings.title = "Take Shortest Path";
		settings.width = 28 * Maze.TS;
		settings.height = 36 * Maze.TS;
		settings.scale = 2;
	}

	@Override
	public void init() {
		PacManGame game = new PacManGame();
		PacManTheme theme = new ClassicPacManTheme();
		PacManGameCast cast = new PacManGameCast(game, theme);
		setController(new TakeShortestPathTestUI(cast));
	}
}