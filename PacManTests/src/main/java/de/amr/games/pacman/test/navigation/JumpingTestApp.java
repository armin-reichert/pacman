package de.amr.games.pacman.test.navigation;

import de.amr.easy.game.Application;
import de.amr.easy.game.config.AppSettings;
import de.amr.games.pacman.controller.actor.Ghost;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.view.play.PlayView;
import de.amr.games.pacman.view.theme.ArcadeTheme;

public class JumpingTestApp extends Application {

	public static void main(String[] args) {
		launch(JumpingTestApp.class, args);
	}

	@Override
	protected void configure(AppSettings settings) {
		settings.width = 28 * Tile.SIZE;
		settings.height = 36 * Tile.SIZE;
		settings.scale = 2;
		settings.title = "Jumping";
	}

	@Override
	public void init() {
		setController(new JumpingTestUI());
	}
}

class JumpingTestUI extends PlayView {

	public JumpingTestUI() {
		super(new Game(), new ArcadeTheme());
		showRoutes = false;
		showStates = true;
		showScores = false;
		showGrid = false;
	}

	@Override
	public void init() {
		super.init();
		game.maze.removeFood();
		game.ghosts().forEach(game.stage::add);
	}

	@Override
	public void update() {
		super.update();
		game.ghostsOnStage().forEach(Ghost::update);
	}
}