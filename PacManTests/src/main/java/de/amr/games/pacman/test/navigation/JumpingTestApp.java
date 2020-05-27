package de.amr.games.pacman.test.navigation;

import de.amr.easy.game.Application;
import de.amr.easy.game.config.AppSettings;
import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.theme.ArcadeTheme;
import de.amr.games.pacman.theme.Theme;
import de.amr.games.pacman.view.play.PlayView;

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
		setController(new JumpingTestUI(new Game(), new ArcadeTheme()));
	}
}

class JumpingTestUI extends PlayView {

	public JumpingTestUI(Game game, Theme theme) {
		super(game, theme);
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