package de.amr.games.pacman.test.navigation;

import de.amr.easy.game.Application;
import de.amr.games.pacman.model.PacManGame;

public class FollowFixedPathTestApp extends Application {

	public static void main(String[] args) {
		launch(new FollowFixedPathTestApp(), args);
	}

	public FollowFixedPathTestApp() {
		settings.title = "Follow Fixed Path";
		settings.width = 28 * PacManGame.TS;
		settings.height = 36 * PacManGame.TS;
		settings.scale = 2;
	}

	@Override
	public void init() {
		setController(new FollowFixedPathTestController());
	}
}