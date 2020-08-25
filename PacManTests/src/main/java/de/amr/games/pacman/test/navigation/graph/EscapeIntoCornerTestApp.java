package de.amr.games.pacman.test.navigation.graph;

import static de.amr.games.pacman.controller.creatures.ghost.GhostState.FRIGHTENED;

import java.util.stream.Stream;

import de.amr.easy.game.Application;
import de.amr.easy.game.config.AppSettings;
import de.amr.games.pacman.controller.creatures.ghost.GhostState;
import de.amr.games.pacman.controller.steering.ghost.FleeingToSafeTile;
import de.amr.games.pacman.model.world.components.Tile;
import de.amr.games.pacman.test.TestController;

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

class EscapeIntoCornerTestUI extends TestController {

	@Override
	public void init() {
		super.init();
		include(pacMan, blinky, inky);
		Stream.of(blinky, inky).forEach(ghost -> ghost.behavior(FRIGHTENED, new FleeingToSafeTile(ghost, pacMan)));
		blinky.ai.setState(FRIGHTENED);
		inky.ai.setState(GhostState.LEAVING_HOUSE);
		inky.nextState = FRIGHTENED;
//		you(pacMan).moveRandomly().ok();
		pacMan.wakeUp();
		view.turnRoutesOn();
		view.turnStatesOn();
	}
}