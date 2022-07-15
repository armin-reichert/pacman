package de.amr.games.pacman.test.navigation;

import de.amr.easy.game.Application;
import de.amr.easy.game.config.AppSettings;
import de.amr.games.pacman.controller.creatures.ghost.GhostState;
import de.amr.games.pacman.controller.steering.api.SteeringBuilder;
import de.amr.games.pacman.lib.Tile;
import de.amr.games.pacman.test.TestController;

public class PortalTestApp extends Application {

	public static void main(String[] args) {
		launch(PortalTestApp.class, args);
	}

	@Override
	protected void configure(AppSettings settings) {
		settings.width = 28 * Tile.TS;
		settings.height = 36 * Tile.TS;
		settings.scale = 2;
		settings.title = "Portal Movement";
	}

	@Override
	public void init() {
		setController(new PortalTestUI());
	}
}

class PortalTestUI extends TestController {

	@Override
	public void init() {
		super.init();
		include(blinky, clyde);
		SteeringBuilder.you(blinky).when(GhostState.CHASING).headFor().tile(Tile.at(0, 17)).ok();
		blinky.ai.setState(GhostState.CHASING);
		SteeringBuilder.you(clyde).when(GhostState.CHASING).headFor().tile(Tile.at(35, 17)).ok();
		clyde.ai.setState(GhostState.LEAVING_HOUSE);
		clyde.nextState = GhostState.CHASING;
		view.turnRoutesOn();
	}
}