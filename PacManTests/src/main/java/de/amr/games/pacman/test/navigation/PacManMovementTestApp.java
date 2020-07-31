package de.amr.games.pacman.test.navigation;

import static de.amr.games.pacman.controller.steering.api.AnimalMaster.you;

import java.awt.Color;
import java.awt.event.KeyEvent;

import de.amr.easy.game.Application;
import de.amr.easy.game.config.AppSettings;
import de.amr.easy.game.input.Keyboard;
import de.amr.games.pacman.controller.event.FoodFoundEvent;
import de.amr.games.pacman.model.world.api.Tile;
import de.amr.games.pacman.test.TestUI;

public class PacManMovementTestApp extends Application {

	public static void main(String[] args) {
		launch(PacManMovementTestApp.class, args);
	}

	@Override
	protected void configure(AppSettings settings) {
		settings.width = 28 * Tile.SIZE;
		settings.height = 36 * Tile.SIZE;
		settings.scale = 2;
		settings.title = "Pac-Man Movement";
	}

	@Override
	public void init() {
		setController(new PacManMovementTestUI());
	}
}

class PacManMovementTestUI extends TestUI {

	private int steeringIndex;

	@Override
	public void init() {
		super.init();
		include(pacMan);
		pacMan.init();
		world.fillFood();
		pacMan.addEventListener(event -> {
			if (event.getClass() == FoodFoundEvent.class) {
				FoodFoundEvent foodFound = (FoodFoundEvent) event;
				world.clearFood(foodFound.location);
				game.level.eatenFoodCount++;
				if (game.level.remainingFoodCount() == 0) {
					world.fillFood();
					game.level.eatenFoodCount = 0;
				}
			}
		});
		view.turnGridOn();
		view.showMessage(1, "SPACE changes steering", Color.WHITE);
		view.showMessage(2, "Cursor keys", Color.WHITE);
		pacMan.wakeUp();
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
			you(pacMan).followTheKeys().keys(KeyEvent.VK_UP, KeyEvent.VK_RIGHT, KeyEvent.VK_DOWN, KeyEvent.VK_LEFT).ok();
			view.showMessage(2, "Cursor keys", Color.WHITE);
		} else if (steeringIndex == 1) {
			you(pacMan).followTheKeys()
					.keys(KeyEvent.VK_NUMPAD8, KeyEvent.VK_NUMPAD6, KeyEvent.VK_NUMPAD2, KeyEvent.VK_NUMPAD4).ok();
			view.showMessage(2, "Numpad keys", Color.WHITE);
		} else if (steeringIndex == 2) {
			you(pacMan).moveRandomly().ok();
			view.showMessage(2, "Move randomly", Color.WHITE);
		}
	}
}