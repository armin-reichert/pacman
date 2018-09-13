package de.amr.games.pacman.test.navigation;

import de.amr.games.pacman.PacManApp;

public class InkyChaseTestApp extends PacManApp {

	public static void main(String[] args) {
		launch(new InkyChaseTestApp());
	}

	public InkyChaseTestApp() {
		super(2);
		settings.title = "Inky Chasing";
	}

	@Override
	public void init() {
		setController(new InkyChaseTestController());
	}
}