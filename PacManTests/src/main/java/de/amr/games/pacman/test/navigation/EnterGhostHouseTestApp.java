package de.amr.games.pacman.test.navigation;

import static de.amr.games.pacman.controller.actor.GhostState.ENTERING_HOUSE;
import static de.amr.games.pacman.controller.actor.GhostState.LEAVING_HOUSE;

import java.awt.Color;
import java.awt.event.KeyEvent;

import de.amr.easy.game.Application;
import de.amr.easy.game.config.AppSettings;
import de.amr.easy.game.input.Keyboard;
import de.amr.games.pacman.model.world.Tile;

public class EnterGhostHouseTestApp extends Application {

	public static void main(String[] args) {
		launch(EnterGhostHouseTestApp.class, args);
	}

	@Override
	protected void configure(AppSettings settings) {
		settings.width = 28 * Tile.SIZE;
		settings.height = 36 * Tile.SIZE;
		settings.scale = 2;
		settings.title = "Enter Ghost House";
	}

	@Override
	public void init() {
		setController(new EnterGhostHouseTestUI());
	}
}

class EnterGhostHouseTestUI extends TestUI {

	public EnterGhostHouseTestUI() {
		showRoutes = true;
		showStates = true;
		showScores = false;
		showGrid = true;
	}

	@Override
	public void init() {
		super.init();
		world.removeFood();
		world.putOnStage(world.inky(), true);
		showMessage("SPACE = Enter/leave house", Color.WHITE, 8);
	}

	@Override
	public void update() {
		super.update();
		if (Keyboard.keyPressedOnce(KeyEvent.VK_SPACE)) {
			world.inky().setState(world.blinky().isInsideHouse() ? LEAVING_HOUSE : ENTERING_HOUSE);
		}
		world.inky().update();
	}
}