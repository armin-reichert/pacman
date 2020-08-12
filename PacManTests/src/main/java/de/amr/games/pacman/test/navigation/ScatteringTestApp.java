package de.amr.games.pacman.test.navigation;

import static de.amr.games.pacman.controller.creatures.ghost.GhostState.SCATTERING;

import java.awt.Color;
import java.awt.event.KeyEvent;

import de.amr.easy.game.Application;
import de.amr.easy.game.config.AppSettings;
import de.amr.easy.game.input.Keyboard;
import de.amr.games.pacman.controller.event.GhostUnlockedEvent;
import de.amr.games.pacman.model.world.api.Tile;
import de.amr.games.pacman.test.TestUI;

public class ScatteringTestApp extends Application {

	public static void main(String[] args) {
		launch(ScatteringTestApp.class, args);
	}

	@Override
	protected void configure(AppSettings settings) {
		settings.width = 28 * Tile.SIZE;
		settings.height = 36 * Tile.SIZE;
		settings.scale = 2;
		settings.title = "Scattering";
	}

	@Override
	public void init() {
		setController(new ScatteringTestUI());
	}
}

class ScatteringTestUI extends TestUI {

	@Override
	public void init() {
		super.init();
		folks.ghosts().forEach(ghost -> {
			world.include(ghost.entity);
			ghost.init();
			ghost.setNextState(SCATTERING);
		});
		view.turnRoutesOn();
		view.turnGridOn();
		view.showMessage(2, "Press SPACE to start", Color.WHITE);
	}

	@Override
	public void update() {
		if (Keyboard.keyPressedOnce(KeyEvent.VK_SPACE)) {
			folks.ghostsInWorld().forEach(ghost -> ghost.ai.process(new GhostUnlockedEvent()));
			view.clearMessage(2);
		}
		if (Keyboard.keyPressedOnce("-")) {
			folks.ghostsInWorld().forEach(ghost -> ghost.reverseDirection());
		}
		super.update();
	}
}