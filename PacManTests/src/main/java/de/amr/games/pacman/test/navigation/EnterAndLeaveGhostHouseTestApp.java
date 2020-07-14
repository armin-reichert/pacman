package de.amr.games.pacman.test.navigation;

import static de.amr.games.pacman.controller.steering.api.SteeringBuilder.headingForTargetTile;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import de.amr.easy.game.Application;
import de.amr.easy.game.config.AppSettings;
import de.amr.games.pacman.controller.creatures.ghost.GhostState;
import de.amr.games.pacman.model.world.core.Bed;
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

	private final List<Tile> capes = Arrays.asList(world.capeNW(), world.capeSE(), world.capeSW());
	private int visits;
	private boolean enteredCape, leftCape;

	private Tile randomCape() {
		return capes.get(new Random().nextInt(capes.size()));
	}

	@Override
	public void init() {
		super.init();
		include(inky);
		inky.init();
		Bed bed = world.theHouse().bed(0);
		inky.placeAt(Tile.at(bed.col(), bed.row()), Tile.SIZE / 2, 0);
		inky.setState(GhostState.SCATTERING);
		view.turnRoutesOn();
		view.turnGridOn();
	}

	@Override
	public void update() {
		if (inky.getState() == GhostState.LEAVING_HOUSE && !inky.isInsideHouse()) {
			inky.setState(GhostState.SCATTERING);
			inky.behavior(GhostState.SCATTERING, headingForTargetTile(inky).tile(this::randomCape).build());
		} else if (inky.getState() == GhostState.SCATTERING) {
			// one round around the block, then killed at cape
			if (capes.contains(inky.location())) {
				enteredCape = true;
			} else {
				if (enteredCape) {
					leftCape = true;
					enteredCape = false;
					visits++;
				}
			}
			if (leftCape && visits == 2) {
				inky.setState(GhostState.DEAD);
				visits = 0;
				enteredCape = leftCape = false;
			}
		}
		super.update();
	}

}