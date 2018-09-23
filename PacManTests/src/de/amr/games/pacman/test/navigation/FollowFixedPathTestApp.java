package de.amr.games.pacman.test.navigation;

import de.amr.games.pacman.PacManApp;
import de.amr.games.pacman.theme.ClassicPacManTheme;

public class FollowFixedPathTestApp extends PacManApp {

	public static void main(String[] args) {
		theme = new ClassicPacManTheme();
		launch(new FollowFixedPathTestApp(), args);
	}

	public FollowFixedPathTestApp() {
		settings.title = "Follow Fixed Path";
	}

	@Override
	public void init() {
		setController(new FollowFixedPathTestController());
	}
}