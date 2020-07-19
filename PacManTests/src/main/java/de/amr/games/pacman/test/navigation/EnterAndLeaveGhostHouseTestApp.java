package de.amr.games.pacman.test.navigation;

import static de.amr.games.pacman.controller.creatures.ghost.GhostState.DEAD;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.LEAVING_HOUSE;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.SCATTERING;
import static de.amr.games.pacman.controller.steering.api.AnimalMaster.you;

import java.util.List;
import java.util.Random;

import de.amr.easy.game.Application;
import de.amr.easy.game.config.AppSettings;
import de.amr.games.pacman.model.world.api.Bed;
import de.amr.games.pacman.model.world.api.Tile;
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

	private final List<Tile> capes = world.capes();
	private int visits;
	private Tile nextCapeToVisit;
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
		inky.setState(SCATTERING);
		view.turnRoutesOn();
		view.turnGridOn();
		nextCapeToVisit = randomCape();
	}

	@Override
	public void update() {
		if (inky.getState() == LEAVING_HOUSE && !inky.isInsideHouse()) {
			inky.setState(SCATTERING);
			you(inky).when(SCATTERING).headFor().tile(nextCapeToVisit).ok();
		} else if (inky.getState() == SCATTERING) {
			// one round around the block, then killed at cape
			if (capes.contains(inky.tileLocation())) {
				enteredCape = true;
			} else {
				if (enteredCape) {
					leftCape = true;
					enteredCape = false;
					visits++;
				}
			}
			if (leftCape && visits == 2) {
				inky.setState(DEAD);
				visits = 0;
				enteredCape = leftCape = false;
				nextCapeToVisit = randomCape();
			}
		}
		super.update();
	}
}