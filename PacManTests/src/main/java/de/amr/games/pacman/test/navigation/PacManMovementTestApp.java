package de.amr.games.pacman.test.navigation;

import java.awt.Color;

import de.amr.games.pacman.PacManApp;

public class PacManMovementTestApp extends PacManApp {

	public static void main(String[] args) {
		launch(new PacManMovementTestApp(), args);
	}

	public PacManMovementTestApp() {
		settings.title = "Pac-Man Movement";
		settings.bgColor = Color.GREEN;
	}

	@Override
	public void init() {
		setController(new PacManMovementTestController());
	}
}