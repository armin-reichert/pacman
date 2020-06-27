package de.amr.games.pacman.test.navigation;

import static de.amr.games.pacman.controller.actor.GhostState.CHASING;

import de.amr.easy.game.Application;
import de.amr.easy.game.config.AppSettings;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.world.Tile;
import de.amr.games.pacman.view.play.PlayView;
import de.amr.games.pacman.view.theme.ArcadeTheme;

/**
 * Test for heading for a tile outside of the maze.
 *
 */
public class OutsideTileTestApp extends Application {

	public static void main(String[] args) {
		launch(OutsideTileTestApp.class, args);
	}

	@Override
	protected void configure(AppSettings settings) {
		settings.width = 28 * Tile.SIZE;
		settings.height = 36 * Tile.SIZE;
		settings.scale = 2;
		settings.title = "Follow Tile Outside Maze";
	}

	@Override
	public void init() {
		setController(new OutsideTileTestUI());
	}
}

class OutsideTileTestUI extends PlayView {

	public OutsideTileTestUI() {
		super(new Game(), new ArcadeTheme());
		showRoutes = true;
		showStates = false;
		showScores = false;
		showGrid = false;
	}

	@Override
	public void init() {
		super.init();
		game.world.eatAllFood();
		theme.snd_ghost_chase().volume(0);
		game.takePart(game.blinky);
		game.blinky.behavior(CHASING,
				game.blinky.isHeadingFor(() -> Tile.col_row(100, game.world.portals().findFirst().get().right.row)));
		game.blinky.setState(CHASING);
	}

	@Override
	public void update() {
		super.update();
		game.blinky.update();
	}
}