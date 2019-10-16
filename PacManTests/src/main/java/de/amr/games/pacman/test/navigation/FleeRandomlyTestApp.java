package de.amr.games.pacman.test.navigation;

import de.amr.games.pacman.PacManApp;

public class FleeRandomlyTestApp extends PacManApp {

	public static void main(String[] args) {
		launch(new FleeRandomlyTestApp(), args);
	}

	public FleeRandomlyTestApp() {
		settings.title = "Flee Randomly";
	}

	@Override
	public void init() {
		setController(new FleeRandomlyTestController());
	}
}