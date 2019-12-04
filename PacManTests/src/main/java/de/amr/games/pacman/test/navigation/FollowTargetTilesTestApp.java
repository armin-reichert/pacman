package de.amr.games.pacman.test.navigation;

import de.amr.easy.game.Application;
import de.amr.games.pacman.actor.Ensemble;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.theme.ClassicPacManTheme;
import de.amr.games.pacman.theme.PacManTheme;

public class FollowTargetTilesTestApp extends Application {

	public static void main(String[] args) {
		launch(new FollowTargetTilesTestApp(), args);
	}

	public FollowTargetTilesTestApp() {
		settings.title = "Follow Target Tiles";
		settings.width = 28 * PacManGame.TS;
		settings.height = 36 * PacManGame.TS;
		settings.scale = 2;
	}

	@Override
	public void init() {
		PacManGame game = new PacManGame();
		PacManTheme theme = new ClassicPacManTheme();
		Ensemble ensemble = new Ensemble(game, theme);
		setController(new FollowTargetTilesTestUI(game, ensemble));
	}
}