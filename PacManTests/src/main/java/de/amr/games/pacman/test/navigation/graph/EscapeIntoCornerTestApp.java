package de.amr.games.pacman.test.navigation.graph;

import static de.amr.games.pacman.controller.actor.GhostState.FRIGHTENED;
import static de.amr.games.pacman.controller.actor.PacManState.EATING;

import de.amr.easy.game.Application;
import de.amr.easy.game.config.AppSettings;
import de.amr.games.pacman.controller.actor.Creature;
import de.amr.games.pacman.model.world.Tile;
import de.amr.games.pacman.test.navigation.TestUI;

public class EscapeIntoCornerTestApp extends Application {

	public static void main(String[] args) {
		launch(EscapeIntoCornerTestApp.class, args);
	}

	@Override
	protected void configure(AppSettings settings) {
		settings.width = 28 * Tile.SIZE;
		settings.height = 36 * Tile.SIZE;
		settings.scale = 2;
		settings.title = "Escape Into Corner";
	}

	@Override
	public void init() {
		setController(new EscapeIntoCornerTestUI());
	}
}

class EscapeIntoCornerTestUI extends TestUI {

	public EscapeIntoCornerTestUI() {
		showRoutes = true;
		showStates = true;
		showScores = false;
	}

	@Override
	public void init() {
		super.init();
		world.eatFood();
		world.pacMan().setState(EATING);
		world.takePart(world.blinky(), true);
		world.blinky().behavior(FRIGHTENED, world.blinky().isFleeingToSafeCorner(world.pacMan()));
		world.blinky().setState(FRIGHTENED);
	}

	@Override
	public void update() {
		super.update();
		world.creaturesOnStage().forEach(Creature::update);
	}
}