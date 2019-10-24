package de.amr.games.pacman.test.navigation;

import de.amr.games.pacman.PacManApp;

public class OutsideTileTestApp extends PacManApp {

	public static void main(String[] args) {
		launch(new OutsideTileTestApp(), args);
	}

	public OutsideTileTestApp() {
		settings.title = "Follow Tile Outside Maze";
	}

	@Override
	public void init() {
		setController(new OutsideTileTestController());
	}
}