package de.amr.games.pacman.test.navigation;

import de.amr.games.pacman.PacManApp;

public class FollowMouseTestApp extends PacManApp {

	public static void main(String[] args) {
		launch(new FollowMouseTestApp());
	}

	public FollowMouseTestApp() {
		settings.title = "Follow Mouse";
	}

	@Override
	public void init() {
		setController(new FollowMouseTestController());
	}
}