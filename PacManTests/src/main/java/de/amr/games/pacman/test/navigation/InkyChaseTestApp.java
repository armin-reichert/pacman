package de.amr.games.pacman.test.navigation;

import static de.amr.easy.game.Application.app;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.CHASING;

import java.awt.Color;
import java.awt.event.KeyEvent;

import de.amr.easy.game.Application;
import de.amr.easy.game.config.AppSettings;
import de.amr.easy.game.input.Keyboard;
import de.amr.games.pacman.controller.event.GhostUnlockedEvent;
import de.amr.games.pacman.model.world.components.Tile;
import de.amr.games.pacman.test.TestController;

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

class InkyChaseTestUI extends TestController {

	@Override
	public void init() {
		super.init();
		app().soundManager().muteAll();
		include(pacMan, inky, blinky);
		pacMan.init();
		folks.ghostsInWorld().forEach(ghost -> {
			ghost.init();
			ghost.nextState = CHASING;
		});
		view.turnRoutesOn();
		view.messagesView.showMessage(1, "Press SPACE to start", Color.WHITE);
	}

	@Override
	public void update() {
		if (Keyboard.keyPressedOnce(KeyEvent.VK_SPACE)) {
			folks.ghostsInWorld().forEach(ghost -> ghost.ai.process(new GhostUnlockedEvent()));
			pacMan.wakeUp();
			view.messagesView.clearMessage(1);
		}
		super.update();
	}
}