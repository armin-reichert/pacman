package de.amr.games.pacman.test.navigation;

import de.amr.easy.game.Application;
import de.amr.games.pacman.actor.PacManGameCast;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.theme.ClassicPacManTheme;
import de.amr.games.pacman.theme.PacManTheme;

public class EscapeIntoCornerTestApp extends Application {

	public static void main(String[] args) {
		launch(new EscapeIntoCornerTestApp(), args);
	}

	public EscapeIntoCornerTestApp() {
		settings.title = "Escape Into Corner";
		settings.width = 28 * Maze.TS;
		settings.height = 36 * Maze.TS;
		settings.scale = 2;
	}

	@Override
	public void init() {
		PacManGame game = new PacManGame();
		PacManTheme theme = new ClassicPacManTheme();
		PacManGameCast ensemble = new PacManGameCast(game, theme);
		setController(new EscapeIntoCornerTestUI(game, ensemble));
	}
}