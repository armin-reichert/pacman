package de.amr.games.pacman.test.navigation.graph;

import static de.amr.games.pacman.controller.creatures.ghost.GhostState.FRIGHTENED;
import static de.amr.games.pacman.controller.steering.api.AnimalMaster.you;

import de.amr.easy.game.Application;
import de.amr.easy.game.config.AppSettings;
import de.amr.games.pacman.controller.creatures.ghost.GhostState;
import de.amr.games.pacman.controller.steering.ghost.FleeingToSafeTile;
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

		blinky.ai.setState(FRIGHTENED);
		inky.nextState = FRIGHTENED;
		inky.ai.setState(GhostState.LEAVING_HOUSE);

		folks.ghostsInWorld().forEach(ghost -> ghost.behavior(FRIGHTENED, new FleeingToSafeTile(ghost, pacMan.body)));

		you(pacMan).moveRandomly().ok();
		pacMan.wakeUp();

		view.turnRoutesOn();
		view.turnStatesOn();
	}
}