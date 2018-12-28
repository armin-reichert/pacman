package de.amr.games.pacman.test.navigation;

import de.amr.games.pacman.PacManApp;

public class EscapeIntoCornerTestApp extends PacManApp {

	public static void main(String[] args) {
		launch(new EscapeIntoCornerTestApp(), args);
	}

	public EscapeIntoCornerTestApp() {
		settings.title = "Escape Into Corner";
	}

	@Override
	public void init() {
		setController(new EscapeIntoCornerTestController());
	}
}