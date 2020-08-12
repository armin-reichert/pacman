package de.amr.games.pacman.test.navigation;

import de.amr.easy.game.Application;
import de.amr.easy.game.config.AppSettings;
import de.amr.games.pacman.controller.creatures.ghost.GhostState;
import de.amr.games.pacman.controller.steering.api.AnimalMaster;
import de.amr.games.pacman.model.world.api.Tile;
import de.amr.games.pacman.test.TestUI;

public class PortalTestApp extends Application {

	public static void main(String[] args) {
		launch(PortalTestApp.class, args);
	}

	@Override
	protected void configure(AppSettings settings) {
		settings.width = 28 * Tile.SIZE;
		settings.height = 36 * Tile.SIZE;
		settings.scale = 2;
		settings.title = "Portal Movement";
	}

	@Override
	public void init() {
		setController(new PortalTestUI());
	}
}

class PortalTestUI extends TestUI {

	@Override
	public void init() {
		super.init();
		include(blinky, clyde);
		AnimalMaster.you(blinky).when(GhostState.CHASING).headFor().tile(Tile.at(0, 17)).ok();
		blinky.ai.setState(GhostState.CHASING);
		AnimalMaster.you(clyde).when(GhostState.CHASING).headFor().tile(Tile.at(35, 17)).ok();
		clyde.ai.setState(GhostState.LEAVING_HOUSE);
		clyde.setNextState(GhostState.CHASING);
		view.turnRoutesOn();
	}
}