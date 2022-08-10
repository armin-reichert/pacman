package de.amr.games.pacman.test.navigation.graph;

import static de.amr.games.pacmanfsm.controller.creatures.ghost.GhostState.FRIGHTENED;

import java.util.stream.Stream;

import de.amr.easy.game.Application;
import de.amr.easy.game.config.AppSettings;
import de.amr.games.pacman.test.TestController;
import de.amr.games.pacmanfsm.PacManApp.PacManAppSettings;
import de.amr.games.pacmanfsm.controller.creatures.ghost.GhostState;
import de.amr.games.pacmanfsm.controller.steering.ghost.FleeingToSafeTile;
import de.amr.games.pacmanfsm.lib.Tile;
import de.amr.games.pacmanfsm.model.world.graph.WorldGraph;

public class EscapeIntoCornerTestApp extends Application {

	public static void main(String[] args) {
		launch(EscapeIntoCornerTestApp.class, new PacManAppSettings(), args);
	}

	@Override
	protected void configure(AppSettings settings) {
		settings.width = 28 * Tile.TS;
		settings.height = 36 * Tile.TS;
		settings.scale = 2;
		settings.title = "Escape To Safe Tile";
	}

	@Override
	public void init() {
		setController(new EscapeIntoCornerTestUI((PacManAppSettings) settings()));
	}
}

class EscapeIntoCornerTestUI extends TestController {

	public EscapeIntoCornerTestUI(PacManAppSettings settings) {
		super(settings);
	}

	@Override
	public void init() {
		super.init();
		include(pacMan, blinky, inky);
		WorldGraph graph = new WorldGraph(settings, world);
		Stream.of(blinky, inky)
				.forEach(ghost -> ghost.setSteering(FRIGHTENED, new FleeingToSafeTile(ghost, graph, pacMan)));
		blinky.ai.setState(FRIGHTENED);
		inky.ai.setState(GhostState.LEAVING_HOUSE);
		inky.nextState = FRIGHTENED;
//		you(pacMan).moveRandomly().ok();
		pacMan.wakeUp();
		view.turnRoutesOn();
		view.turnStatesOn();
	}
}