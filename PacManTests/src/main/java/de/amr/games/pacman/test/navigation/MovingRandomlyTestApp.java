package de.amr.games.pacman.test.navigation;

import static de.amr.games.pacman.model.PacManGame.TS;

import de.amr.easy.game.Application;
import de.amr.games.pacman.actor.PacManGameCast;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.theme.ClassicPacManTheme;
import de.amr.games.pacman.theme.PacManTheme;

public class MovingRandomlyTestApp extends Application {

	public static void main(String[] args) {
		launch(new MovingRandomlyTestApp(), args);
	}

	public MovingRandomlyTestApp() {
		settings.title = "Moving Randomly";
		settings.width = 28 * TS;
		settings.height = 36 * TS;
		settings.scale = 2;
	}

	@Override
	public void init() {
		PacManGame game = new PacManGame();
		PacManTheme theme = new ClassicPacManTheme();
		PacManGameCast ensemble = new PacManGameCast(game, theme);
		setController(new MovingRandomlyTestUI(game, ensemble));
	}
}