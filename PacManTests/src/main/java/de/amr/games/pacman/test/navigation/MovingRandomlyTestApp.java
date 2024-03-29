package de.amr.games.pacman.test.navigation;

import static de.amr.games.pacmanfsm.controller.creatures.ghost.GhostState.FRIGHTENED;
import static de.amr.games.pacmanfsm.controller.steering.api.SteeringBuilder.you;

import java.awt.Color;
import java.awt.event.KeyEvent;

import de.amr.easy.game.Application;
import de.amr.easy.game.config.AppSettings;
import de.amr.easy.game.input.Keyboard;
import de.amr.games.pacman.test.TestController;
import de.amr.games.pacmanfsm.PacManApp.PacManAppSettings;
import de.amr.games.pacmanfsm.lib.Tile;
import de.amr.games.pacmanfsm.model.world.components.Bed;

public class MovingRandomlyTestApp extends Application {

	public static void main(String[] args) {
		launch(MovingRandomlyTestApp.class, new PacManAppSettings(), args);
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
		setController(new MovingRandomlyTestUI((PacManAppSettings) settings()));
	}
}

class MovingRandomlyTestUI extends TestController {

	public MovingRandomlyTestUI(PacManAppSettings settings) {
		super(settings);
	}

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
			ghost.tf.centerBoth(bed.minX() * Tile.TS, bed.minY() * Tile.TS, bed.width() * Tile.TS, bed.height() * Tile.TS);
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