package de.amr.games.pacman.test.navigation;

import de.amr.easy.game.Application;
import de.amr.easy.game.config.AppSettings;
import de.amr.games.pacman.controller.actor.GhostState;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.world.PacManWorld;
import de.amr.games.pacman.model.world.Tile;
import de.amr.games.pacman.model.world.Worlds;
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
		setController(new OutsideTileTestUI(Worlds.arcade()));
	}
}

class OutsideTileTestUI extends PlayView {

	public OutsideTileTestUI(PacManWorld world) {
		super(world, new Game(world, 1), new ArcadeTheme());
		showRoutes = true;
		showStates = false;
		showScores = false;
		showGrid = false;
	}

	@Override
	public void init() {
		super.init();
		world.eatFood();
		theme.snd_ghost_chase().volume(0);
		world.takePart(world.blinky);
		int row = world.portals().findFirst().map(portal -> portal.right.row).orElse((short) 100);
		world.blinky.behavior(GhostState.CHASING, world.blinky.isHeadingFor(() -> Tile.at(100, row)));
		world.blinky.setState(GhostState.CHASING);
	}

	@Override
	public void update() {
		super.update();
		world.blinky.update();
	}
}