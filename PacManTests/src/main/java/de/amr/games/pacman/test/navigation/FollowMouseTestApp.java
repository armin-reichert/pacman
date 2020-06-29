package de.amr.games.pacman.test.navigation;

import static de.amr.games.pacman.controller.actor.GhostState.CHASING;

import de.amr.easy.game.Application;
import de.amr.easy.game.config.AppSettings;
import de.amr.easy.game.input.Mouse;
import de.amr.games.pacman.model.world.Tile;

public class FollowMouseTestApp extends Application {

	public static void main(String[] args) {
		launch(FollowMouseTestApp.class, args);
	}

	@Override
	protected void configure(AppSettings settings) {
		settings.width = 28 * Tile.SIZE;
		settings.height = 36 * Tile.SIZE;
		settings.scale = 2;
		settings.title = "Blinky Follows Mouse";
	}

	@Override
	public void init() {
		setController(new FollowMouseTestUI());
	}
}

class FollowMouseTestUI extends TestUI {

	private Tile mousePosition = Tile.at(0, 0);

	public FollowMouseTestUI() {
		showRoutes = true;
		showStates = false;
		showScores = false;
		showGrid = true;
	}

	@Override
	public void init() {
		super.init();
		world.eatFood();
		world.takePart(world.blinky(), true);
		world.blinky().behavior(CHASING, world.blinky().isHeadingFor(() -> mousePosition));
		world.blinky().setState(CHASING);
	}

	@Override
	public void update() {
		super.update();
		if (Mouse.moved()) {
			mousePosition = Tile.at(Mouse.getX() / Tile.SIZE, Mouse.getY() / Tile.SIZE);
		}
		world.blinky().update();
	}
}