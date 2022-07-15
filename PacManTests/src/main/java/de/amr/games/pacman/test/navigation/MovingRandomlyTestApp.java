package de.amr.games.pacman.test.navigation;

import static de.amr.games.pacman.controller.creatures.ghost.GhostState.FRIGHTENED;
import static de.amr.games.pacman.controller.steering.api.SteeringBuilder.you;

import java.awt.Color;
import java.awt.event.KeyEvent;

import de.amr.easy.game.Application;
import de.amr.easy.game.config.AppSettings;
import de.amr.easy.game.input.Keyboard;
import de.amr.games.pacman.lib.Tile;
import de.amr.games.pacman.model.world.components.Bed;
import de.amr.games.pacman.test.TestController;

public class MovingRandomlyTestApp extends Application {

	public static void main(String[] args) {
		launch(MovingRandomlyTestApp.class, args);
	}

	@Override
	protected void configure(AppSettings settings) {
		settings.width = 28 * Tile.TS;
		settings.height = 36 * Tile.TS;
		settings.scale = 2;
		settings.title = "Moving Randomly";
	}

	@Override
	public void init() {
		setController(new MovingRandomlyTestUI());
	}
}

class MovingRandomlyTestUI extends TestController {

	private boolean started;

	@Override
	public void init() {
		super.init();
		include(blinky, pinky, inky, clyde);
		folks.ghostsInWorld().forEach(ghost -> {
			ghost.init();
			you(ghost).when(FRIGHTENED).moveRandomly().ok();
			ghost.ai.state(FRIGHTENED).removeTimer();
			ghost.ai.setState(FRIGHTENED);
			Bed bed = world.pacManBed();
			ghost.tf.centerBoth(bed.col() * Tile.TS, bed.row() * Tile.TS, bed.width() * Tile.TS,
					bed.height() * Tile.TS);
		});
		view.turnRoutesOn();
		view.turnStatesOn();
		view.turnGridOn();
		view.messagesView.showMessage(1, "Press SPACE", Color.WHITE);
		started = false;
	}

	@Override
	public void update() {
		if (!started && Keyboard.keyPressedOnce(KeyEvent.VK_SPACE)) {
			started = true;
			view.messagesView.clearMessage(1);
		}
		if (started) {
			super.update();
		}
	}
}