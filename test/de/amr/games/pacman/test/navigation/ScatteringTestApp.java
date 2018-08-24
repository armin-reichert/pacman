package de.amr.games.pacman.test.navigation;

import de.amr.easy.game.Application;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.theme.PacManTheme;

public class ScatteringTestApp extends Application {

	public static void main(String[] args) {
		launch(new ScatteringTestApp());
	}

	public ScatteringTestApp() {
		settings.width = 28 * Game.TS;
		settings.height = 36 * Game.TS;
		settings.scale = 2;
		settings.title = "Scattering";
	}

	@Override
	public void init() {
		PacManTheme.init();
		setController(new ScatteringTestView(settings.width, settings.height));
	}
}