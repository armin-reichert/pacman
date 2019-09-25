package de.amr.games.pacman.test.navigation;

import de.amr.games.pacman.PacManApp;

public class FollowFixedPathTestApp extends PacManApp {

	public static void main(String[] args) {
		launch(new FollowFixedPathTestApp(), args);
	}

	public FollowFixedPathTestApp() {
		settings.title = "Follow Fixed Path";
	}

	@Override
	public void init() {
		setController(new FollowFixedPathTestController(theme));
	}
}