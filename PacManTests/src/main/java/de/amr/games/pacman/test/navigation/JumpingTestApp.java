package de.amr.games.pacman.test.navigation;

import de.amr.easy.game.Application;
import de.amr.easy.game.config.AppSettings;
import de.amr.games.pacman.controller.actor.Ghost;
import de.amr.games.pacman.model.world.Tile;

public class JumpingTestApp extends Application {

	public static void main(String[] args) {
		launch(JumpingTestApp.class, args);
	}

	@Override
	protected void configure(AppSettings settings) {
		settings.width = 28 * Tile.SIZE;
		settings.height = 36 * Tile.SIZE;
		settings.scale = 2;
		settings.title = "Jumping";
	}

	@Override
	public void init() {
		setController(new JumpingTestUI());
	}
}

class JumpingTestUI extends TestUI {

	public JumpingTestUI() {
		showRoutes = false;
		showStates = true;
		showScores = false;
		showGrid = false;
	}

	@Override
	public void init() {
		super.init();
		world.removeFood();
		world.ghosts().forEach(ghost -> world.putOnStage(ghost, true));
	}

	@Override
	public void update() {
		super.update();
		world.ghostsOnStage().forEach(Ghost::update);
	}
}