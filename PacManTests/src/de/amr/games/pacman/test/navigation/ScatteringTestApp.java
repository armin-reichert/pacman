package de.amr.games.pacman.test.navigation;

import de.amr.games.pacman.PacManApp;

public class ScatteringTestApp extends PacManApp {

	public static void main(String[] args) {
		launch(new ScatteringTestApp(), args);
	}

	public ScatteringTestApp() {
		settings.title = "Scattering";
	}

	@Override
	public void init() {
		setController(new ScatteringTestController());
	}
}