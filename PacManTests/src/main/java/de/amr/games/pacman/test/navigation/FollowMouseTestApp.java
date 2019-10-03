package de.amr.games.pacman.test.navigation;

import de.amr.games.pacman.PacManApp;
import de.amr.games.pacman.theme.ClassicPacManTheme;

public class FollowMouseTestApp extends PacManApp {

	public static void main(String[] args) {
		launch(new FollowMouseTestApp(), args);
	}

	public FollowMouseTestApp() {
		super(new ClassicPacManTheme());
		settings.title = "Follow Mouse";
	}

	@Override
	public void init() {
		setController(new FollowMouseTestController(theme));
	}
}