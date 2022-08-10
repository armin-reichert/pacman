package de.amr.games.pacman.test.navigation;

import static de.amr.easy.game.Application.app;
import static de.amr.games.pacmanfsm.controller.creatures.ghost.GhostState.CHASING;

import java.awt.Color;
import java.awt.event.KeyEvent;

import de.amr.easy.game.Application;
import de.amr.easy.game.config.AppSettings;
import de.amr.easy.game.input.Keyboard;
import de.amr.games.pacman.test.TestController;
import de.amr.games.pacmanfsm.PacManApp.PacManAppSettings;
import de.amr.games.pacmanfsm.controller.event.GhostUnlockedEvent;
import de.amr.games.pacmanfsm.lib.Tile;

public class InkyChaseTestApp extends Application {

	public static void main(String[] args) {
		launch(InkyChaseTestApp.class, new PacManAppSettings(), args);
	}

	@Override
	protected void configure(AppSettings settings) {
		settings.width = 28 * Tile.TS;
		settings.height = 36 * Tile.TS;
		settings.scale = 2;
		settings.title = "Inky Chasing";
	}

	@Override
	public void init() {
		setController(new InkyChaseTestUI((PacManAppSettings) settings()));
	}
}

class InkyChaseTestUI extends TestController {

	public InkyChaseTestUI(PacManAppSettings settings) {
		super(settings);
	}

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