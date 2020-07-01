package de.amr.games.pacman.test.navigation;

import static de.amr.games.pacman.controller.actor.GhostState.ENTERING_HOUSE;
import static de.amr.games.pacman.controller.actor.GhostState.LEAVING_HOUSE;

import java.awt.Color;
import java.awt.event.KeyEvent;

import de.amr.easy.game.Application;
import de.amr.easy.game.config.AppSettings;
import de.amr.easy.game.input.Keyboard;
import de.amr.games.pacman.model.world.core.Tile;

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
		view.showRoutes = true;
		view.showStates = true;
		view.showGrid = true;
	}

	@Override
	public void init() {
		super.init();
		include(inky);
		view.showMessage("SPACE = Enter/leave house", Color.WHITE, 8);
	}

	@Override
	public void update() {
		if (Keyboard.keyPressedOnce(KeyEvent.VK_SPACE)) {
			inky.setState(blinky.isInsideHouse() ? LEAVING_HOUSE : ENTERING_HOUSE);
		}
		super.update();
	}
}