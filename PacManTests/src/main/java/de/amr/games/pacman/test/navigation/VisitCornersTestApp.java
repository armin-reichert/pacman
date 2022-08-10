package de.amr.games.pacman.test.navigation;

import static de.amr.easy.game.Application.app;
import static de.amr.games.pacmanfsm.controller.creatures.ghost.GhostState.CHASING;
import static de.amr.games.pacmanfsm.controller.steering.api.SteeringBuilder.you;

import java.util.Arrays;
import java.util.List;

import de.amr.easy.game.Application;
import de.amr.easy.game.config.AppSettings;
import de.amr.games.pacman.test.TestController;
import de.amr.games.pacmanfsm.PacManApp.PacManAppSettings;
import de.amr.games.pacmanfsm.lib.Tile;

public class VisitCornersTestApp extends Application {

	public static void main(String[] args) {
		launch(VisitCornersTestApp.class, new PacManAppSettings(), args);
	}

	@Override
	protected void configure(AppSettings settings) {
		settings.width = 28 * Tile.TS;
		settings.height = 36 * Tile.TS;
		settings.scale = 2;
		settings.title = "Visit Corners";
	}

	@Override
	public void init() {
		setController(new FollowTargetTilesTestUI((PacManAppSettings) settings()));
	}
}

class FollowTargetTilesTestUI extends TestController {

	private List<Tile> targets;
	private int current;

	public FollowTargetTilesTestUI(PacManAppSettings settings) {
		super(settings);
	}

	@Override
	public void init() {
		super.init();
		List<Tile> capes = world.capes();
		targets = Arrays.asList(capes.get(0), capes.get(1), capes.get(2),
				Tile.at(world.pacManBed().minX(), world.pacManBed().minY()), capes.get(3));
		current = 0;
		app().soundManager().muteAll();
		include(blinky);
		blinky.init();
		blinky.placeAt(targets.get(0), 0, 0);
		you(blinky).when(CHASING).headFor().tile(() -> targets.get(current)).ok();
		blinky.ai.setState(CHASING);
		blinky.getSteering().force();
		view.turnRoutesOn();
		view.turnGridOn();
	}

	@Override
	public void update() {
		if (blinky.tile().equals(targets.get(current))) {
			current += 1;
			if (current == targets.size()) {
				current = 0;
			}
		}
		super.update();
	}
}