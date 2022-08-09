package de.amr.games.pacman.test.navigation;

import static de.amr.easy.game.Application.app;
import static de.amr.games.pacmanfsm.controller.creatures.ghost.GhostState.CHASING;
import static de.amr.games.pacmanfsm.controller.steering.api.SteeringBuilder.you;

import de.amr.easy.game.Application;
import de.amr.easy.game.config.AppSettings;
import de.amr.games.pacman.test.TestController;
import de.amr.games.pacmanfsm.lib.Tile;

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
		settings.width = 28 * Tile.TS;
		settings.height = 36 * Tile.TS;
		settings.scale = 2;
		settings.title = "Follow Tile Outside Maze";
	}

	@Override
	public void init() {
		setController(new OutsideTileTestUI());
	}
}

class OutsideTileTestUI extends TestController {

	@Override
	public void init() {
		super.init();
		app().soundManager().muteAll();
		include(blinky);
		blinky.init();
		int row = world.portals().findFirst().map(portal -> portal.other.row).orElse((short) 100);
		you(blinky).when(CHASING).headFor().tile(100, row).ok();
		blinky.ai.setState(CHASING);
		view.turnRoutesOn();
	}
}