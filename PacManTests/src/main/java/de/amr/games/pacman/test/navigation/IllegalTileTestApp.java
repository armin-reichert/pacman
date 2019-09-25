package de.amr.games.pacman.test.navigation;

import de.amr.games.pacman.PacManApp;

public class IllegalTileTestApp extends PacManApp {

	public static void main(String[] args) {
		launch(new IllegalTileTestApp(), args);
	}

	public IllegalTileTestApp() {
		settings.title = "Follow Illegal Tile";
	}

	@Override
	public void init() {
		setController(new IllegalTileTestController(theme));
	}
}