package de.amr.games.pacman.test.navigation;

import de.amr.easy.game.Application;
import de.amr.easy.game.view.View;
import de.amr.easy.game.view.VisualController;
import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.actor.PacManGameCast;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.theme.ClassicPacManTheme;
import de.amr.games.pacman.theme.PacManTheme;
import de.amr.games.pacman.view.play.PlayView;

public class JumpingTestApp extends Application {

	public static void main(String[] args) {
		launch(new JumpingTestApp(), args);
	}

	public JumpingTestApp() {
		settings.title = "Jumping";
		settings.width = 28 * Tile.SIZE;
		settings.height = 36 * Tile.SIZE;
		settings.scale = 2;
	}

	@Override
	public void init() {
		PacManGame game = new PacManGame();
		PacManTheme theme = new ClassicPacManTheme();
		PacManGameCast cast = new PacManGameCast(game, theme);
		setController(new JumpingTestUI(cast));
	}
}

class JumpingTestUI extends PlayView implements VisualController {

	public JumpingTestUI(PacManGameCast cast) {
		super(cast);
		showRoutes(false);
		showStates(true);
		setShowScores(false);
		showGrid(false);
	}

	@Override
	public void init() {
		super.init();
		game().init();
		maze().removeFood();
		cast().ghosts().forEach(cast()::putOnStage);
	}

	@Override
	public void update() {
		cast().ghostsOnStage().forEach(Ghost::update);
		super.update();
	}

	@Override
	public View currentView() {
		return this;
	}
}