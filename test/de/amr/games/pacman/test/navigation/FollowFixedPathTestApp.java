package de.amr.games.pacman.test.navigation;

import de.amr.easy.game.Application;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.theme.ClassicPacManTheme;
import de.amr.games.pacman.theme.PacManThemes;

public class FollowFixedPathTestApp extends Application {

	public static void main(String[] args) {
		launch(new FollowFixedPathTestApp());
	}

	public FollowFixedPathTestApp() {
		settings.width = 28 * Game.TS;
		settings.height = 36 * Game.TS;
		settings.scale = 2;
		settings.title = "Follow Fixed Path";
	}

	@Override
	public void init() {
		PacManThemes.use(ClassicPacManTheme.class);
		setController(new FollowFixedPathTestController(settings.width, settings.height));
	}

}
