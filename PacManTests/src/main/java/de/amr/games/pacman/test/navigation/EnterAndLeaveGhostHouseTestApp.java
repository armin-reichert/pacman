package de.amr.games.pacman.test.navigation;

import java.util.Arrays;
import java.util.Random;

import de.amr.easy.game.Application;
import de.amr.easy.game.config.AppSettings;
import de.amr.games.pacman.controller.actor.GhostState;
import de.amr.games.pacman.model.world.core.Tile;
import de.amr.games.pacman.test.TestUI;

public class EnterAndLeaveGhostHouseTestApp extends Application {

	public static void main(String[] args) {
		launch(EnterAndLeaveGhostHouseTestApp.class, args);
	}

	@Override
	protected void configure(AppSettings settings) {
		settings.width = 28 * Tile.SIZE;
		settings.height = 36 * Tile.SIZE;
		settings.scale = 2;
		settings.title = "Enter/leave Ghost House";
	}

	@Override
	public void init() {
		setController(new EnterGhostHouseTestUI());
	}
}

class EnterGhostHouseTestUI extends TestUI {

	Tile[] capes = { world.capeNW(), world.capeSE(), world.capeSW() };

	private Tile randomCape() {
		return capes[new Random().nextInt(capes.length)];
	}

	@Override
	public void init() {
		super.init();
		include(inky);
		inky.placeAt(world.theHouse().bed(0).tile);
		inky.setState(GhostState.SCATTERING);
		view.showingRoutes = true;
		view.showingGrid = true;
	}

	@Override
	public void update() {
		if (inky.getState() == GhostState.LEAVING_HOUSE && !inky.isInsideHouse()) {
			inky.setState(GhostState.SCATTERING);
			inky.behavior(GhostState.SCATTERING, inky.headingFor(randomCape()));
		} else if (inky.getState() == GhostState.SCATTERING && Arrays.asList(capes).contains(inky.tile())) {
			inky.setState(GhostState.DEAD);
		}
		super.update();
	}

}