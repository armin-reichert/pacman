package de.amr.games.pacman.test.navigation;

import de.amr.easy.game.Application;
import de.amr.easy.game.config.AppSettings;
import de.amr.games.pacman.test.TestController;
import de.amr.games.pacmanfsm.controller.creatures.ghost.Ghost;
import de.amr.games.pacmanfsm.lib.Tile;

public class JumpingTestApp extends Application {

	public static void main(String[] args) {
		launch(JumpingTestApp.class, args);
	}

	@Override
	protected void configure(AppSettings settings) {
		settings.width = 28 * Tile.TS;
		settings.height = 36 * Tile.TS;
		settings.scale = 2;
		settings.title = "Jumping";
	}

	@Override
	public void init() {
		setController(new JumpingTestUI());
	}
}

class JumpingTestUI extends TestController {

	@Override
	public void init() {
		super.init();
		include(blinky, inky, pinky, clyde);
		folks.ghostsInWorld().forEach(Ghost::init);
		view.turnGridOn();
		view.turnStatesOn();
	}
}