package de.amr.games.pacman.test.navigation;

import static de.amr.games.pacman.controller.creatures.ghost.GhostState.CHASING;

import java.awt.Color;
import java.awt.event.KeyEvent;

import de.amr.easy.game.Application;
import de.amr.easy.game.config.AppSettings;
import de.amr.easy.game.input.Keyboard;
import de.amr.games.pacman.controller.event.GhostUnlockedEvent;
import de.amr.games.pacman.model.world.api.Tile;
import de.amr.games.pacman.test.TestUI;

public class InkyChaseTestApp extends Application {

	public static void main(String[] args) {
		launch(InkyChaseTestApp.class, args);
	}

	@Override
	protected void configure(AppSettings settings) {
		settings.width = 28 * Tile.SIZE;
		settings.height = 36 * Tile.SIZE;
		settings.scale = 2;
		settings.title = "Inky Chasing";
	}

	@Override
	public void init() {
		setController(new InkyChaseTestUI());
	}
}

class InkyChaseTestUI extends TestUI {

	@Override
	public void init() {
		super.init();
		soundManager.snd_ghost_chase().volume(0);
		include(pacMan, inky, blinky);
		pacMan.init();
		ghostsOnStage().forEach(ghost -> {
			ghost.init();
			ghost.nextStateToEnter(() -> CHASING);
		});
		view.turnRoutesOn();
		view.showMessage(1, "Press SPACE to start", Color.WHITE);
	}

	@Override
	public void update() {
		if (Keyboard.keyPressedOnce(KeyEvent.VK_SPACE)) {
			ghostsOnStage().forEach(ghost -> ghost.process(new GhostUnlockedEvent()));
			pacMan.startRunning();
			view.clearMessage(1);
		}
		super.update();
	}
}