package de.amr.games.pacman.test.navigation;

import de.amr.easy.game.Application;
import de.amr.games.pacman.model.PacManGame;

public class ScatteringTestApp extends Application {

	public static void main(String[] args) {
		launch(new ScatteringTestApp(), args);
	}

	public ScatteringTestApp() {
		settings.title = "Scattering";
		settings.width = 28 * PacManGame.TS;
		settings.height = 36 * PacManGame.TS;
		settings.scale = 2;
	}

	@Override
	public void init() {
		setController(new ScatteringTestController());
	}
}