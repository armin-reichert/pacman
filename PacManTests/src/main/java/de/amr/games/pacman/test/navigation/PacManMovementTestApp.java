package de.amr.games.pacman.test.navigation;

import static de.amr.games.pacmanfsm.controller.steering.api.SteeringBuilder.you;

import java.awt.Color;
import java.awt.event.KeyEvent;

import de.amr.easy.game.Application;
import de.amr.easy.game.config.AppSettings;
import de.amr.easy.game.input.Keyboard;
import de.amr.games.pacman.test.TestController;
import de.amr.games.pacmanfsm.PacManApp.PacManAppSettings;
import de.amr.games.pacmanfsm.controller.event.FoodFoundEvent;
import de.amr.games.pacmanfsm.controller.game.GameController;
import de.amr.games.pacmanfsm.lib.Tile;

public class PacManMovementTestApp extends Application {

	public static void main(String[] args) {
		launch(PacManMovementTestApp.class, new PacManAppSettings(), args);
	}

	@Override
	protected void configure(AppSettings settings) {
		settings.width = 28 * Tile.TS;
		settings.height = 36 * Tile.TS;
		settings.scale = 2;
		settings.title = "Pac-Man Movement";
	}

	@Override
	public void init() {
		setController(new PacManMovementTestUI((PacManAppSettings) settings()));
	}
}

class PacManMovementTestUI extends TestController {

	private int steeringIndex;

	public PacManMovementTestUI(PacManAppSettings settings) {
		super(settings);
	}

	@Override
	public void init() {
		super.init();
		world.restoreFood();
		include(pacMan);
		pacMan.ai.addEventListener(event -> {
			if (event.getClass() == FoodFoundEvent.class) {
				FoodFoundEvent foodFound = (FoodFoundEvent) event;
				world.removeFood(foodFound.location);
				GameController.theGame().eatenFoodCount++;
				if (GameController.theGame().remainingFoodCount() == 0) {
					world.restoreFood();
					GameController.theGame().eatenFoodCount = 0;
				}
			}
		});
		pacMan.init();
		pacMan.wakeUp();
		view.turnGridOn();
		view.messagesView.showMessage(1, "SPACE changes steering", Color.WHITE);
		view.messagesView.showMessage(2, "Cursor keys", Color.WHITE);
	}

	@Override
	public void update() {
		if (Keyboard.keyPressedOnce(KeyEvent.VK_SPACE)) {
			steeringIndex = (steeringIndex + 1) % 3;
			changeSteering();
		}
		super.update();
	}

	private void changeSteering() {
		if (steeringIndex == 0) {
			you(pacMan).followTheCursorKeys().ok();
			view.messagesView.showMessage(2, "Cursor keys", Color.WHITE);
		} else if (steeringIndex == 1) {
			you(pacMan).followTheKeys()
					.keys(KeyEvent.VK_NUMPAD8, KeyEvent.VK_NUMPAD6, KeyEvent.VK_NUMPAD2, KeyEvent.VK_NUMPAD4).ok();
			view.messagesView.showMessage(2, "Numpad keys", Color.WHITE);
		} else if (steeringIndex == 2) {
			you(pacMan).moveRandomly().ok();
			view.messagesView.showMessage(2, "Move randomly", Color.WHITE);
		}
	}
}