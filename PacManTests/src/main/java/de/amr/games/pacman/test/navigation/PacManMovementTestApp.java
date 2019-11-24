package de.amr.games.pacman.test.navigation;

import de.amr.easy.game.Application;
import de.amr.games.pacman.model.PacManGame;

public class PacManMovementTestApp extends Application {

	public static void main(String[] args) {
		launch(new PacManMovementTestApp(), args);
	}

	public PacManMovementTestApp() {
		settings.title = "Pac-Man Movement";
		settings.width = 28 * PacManGame.TS;
		settings.height = 36 * PacManGame.TS;
		settings.scale = 2;
	}

	@Override
	public void init() {
		setController(new PacManMovementTestUI(new PacManGame()));
	}
}