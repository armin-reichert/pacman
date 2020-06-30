package de.amr.games.pacman.test.navigation;

import static de.amr.games.pacman.controller.actor.GhostState.FRIGHTENED;

import java.awt.Color;
import java.awt.event.KeyEvent;

import de.amr.easy.game.Application;
import de.amr.easy.game.config.AppSettings;
import de.amr.easy.game.input.Keyboard;
import de.amr.games.pacman.model.world.Tile;

public class MovingRandomlyTestApp extends Application {

	public static void main(String[] args) {
		launch(MovingRandomlyTestApp.class, args);
	}

	@Override
	protected void configure(AppSettings settings) {
		settings.width = 28 * Tile.SIZE;
		settings.height = 36 * Tile.SIZE;
		settings.scale = 2;
		settings.title = "Moving Randomly";
	}

	@Override
	public void init() {
		setController(new MovingRandomlyTestUI());
	}
}

class MovingRandomlyTestUI extends TestUI {

	boolean started;

	public MovingRandomlyTestUI() {
		view.showRoutes = true;
		view.showStates = true;
		view.showGrid = true;
	}

	@Override
	public void init() {
		super.init();
		putOnStage(blinky, pinky, inky, clyde);
		ghostsOnStage().forEach(ghost -> {
			ghost.tf.setPosition(pacMan.bed().position);
			ghost.behavior(FRIGHTENED, ghost.movingRandomly());
			ghost.state(FRIGHTENED).removeTimer();
			ghost.setState(FRIGHTENED);
		});
		view.showMessage("Press SPACE", Color.WHITE);
	}

	@Override
	public void update() {
		if (Keyboard.keyPressedOnce(KeyEvent.VK_SPACE)) {
			started = true;
			view.clearMessage();
		}
		if (started) {
			super.update();
		}
	}
}