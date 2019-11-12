package de.amr.games.pacman.test.navigation;

import de.amr.games.pacman.PacManApp;

public class JumpingTestApp extends PacManApp {

	public static void main(String[] args) {
		launch(new JumpingTestApp(), args);
	}

	public JumpingTestApp() {
		settings.title = "Jumping";
	}

	@Override
	public void init() {
		setController(new JumpingTestController());
	}
}