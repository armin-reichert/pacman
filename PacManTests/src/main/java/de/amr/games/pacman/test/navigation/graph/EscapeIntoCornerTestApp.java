package de.amr.games.pacman.test.navigation.graph;

import static de.amr.games.pacman.controller.creatures.ghost.GhostState.FRIGHTENED;
import static de.amr.games.pacman.controller.steering.api.AnimalMaster.you;

import de.amr.easy.game.Application;
import de.amr.easy.game.config.AppSettings;
import de.amr.games.pacman.controller.creatures.ghost.GhostState;
import de.amr.games.pacman.model.world.api.Tile;
import de.amr.games.pacman.test.TestUI;

public class EscapeIntoCornerTestApp extends Application {

	public static void main(String[] args) {
		launch(EscapeIntoCornerTestApp.class, args);
	}

	@Override
	protected void configure(AppSettings settings) {
		settings.width = 28 * Tile.SIZE;
		settings.height = 36 * Tile.SIZE;
		settings.scale = 2;
		settings.title = "Escape To Safe Tile";
	}

	@Override
	public void init() {
		setController(new EscapeIntoCornerTestUI());
	}
}

class EscapeIntoCornerTestUI extends TestUI {

	@Override
	public void init() {
		super.init();
		include(pacMan, blinky, inky);

		you(blinky).when(FRIGHTENED).fleeToSafeTile().from(pacMan).ok();
		blinky.setState(FRIGHTENED);

		you(inky).when(FRIGHTENED).fleeToSafeTile().from(pacMan).ok();
		inky.setNextStateToEnter(() -> FRIGHTENED);
		inky.setState(GhostState.LEAVING_HOUSE);

		you(pacMan).moveRandomly().ok();
		pacMan.wakeUp();

		view.turnRoutesOn();
		view.turnStatesOn();
	}
}