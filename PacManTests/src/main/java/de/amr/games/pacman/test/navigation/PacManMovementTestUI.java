package de.amr.games.pacman.test.navigation;

import java.awt.event.KeyEvent;

import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.view.View;
import de.amr.easy.game.view.ViewController;
import de.amr.games.pacman.actor.Actor;
import de.amr.games.pacman.actor.PacManGameCast;
import de.amr.games.pacman.actor.behavior.pacman.PacManSteerings;
import de.amr.games.pacman.controller.event.FoodFoundEvent;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.view.play.PlayView;

public class PacManMovementTestUI extends PlayView implements ViewController {

	public PacManMovementTestUI(PacManGame game, PacManGameCast ensemble) {
		super(game, ensemble);
		showScores = false;
	}

	@Override
	public void init() {
		super.init();
		game.start();
		cast.pacMan.addListener(event -> {
			if (event.getClass() == FoodFoundEvent.class) {
				FoodFoundEvent foodFound = (FoodFoundEvent) event;
				cast.theme.snd_eatPill().play();
				game.maze.removeFood(foodFound.tile);
				if (game.maze.tiles().filter(game.maze::containsFood).count() == 0) {
					game.maze.restoreFood();
				}
			}
		});
		cast.pacMan.activate();
		cast.pacMan.init();
	}

	@Override
	public void update() {
		handleSteeringChange();
		cast.activeActors().forEach(Actor::update);
		super.update();
	}

	private void handleSteeringChange() {
		if (Keyboard.keyPressedOnce(KeyEvent.VK_M)) {
			cast.pacMan.steering = PacManSteerings.steeredByKeys(KeyEvent.VK_UP, KeyEvent.VK_RIGHT,
					KeyEvent.VK_DOWN, KeyEvent.VK_LEFT);
		}
		else if (Keyboard.keyPressedOnce(KeyEvent.VK_N)) {
			cast.pacMan.steering = PacManSteerings.steeredByKeys(KeyEvent.VK_NUMPAD5, KeyEvent.VK_NUMPAD3,
					KeyEvent.VK_NUMPAD2, KeyEvent.VK_NUMPAD1);
		}
		else if (Keyboard.keyPressedOnce(KeyEvent.VK_A)) {
			cast.pacMan.steering = PacManSteerings.avoidGhosts();
		}
		else if (Keyboard.keyPressedOnce(KeyEvent.VK_R)) {
			cast.pacMan.steering = PacManSteerings.movingRandomly();
		}
	}

	@Override
	public View currentView() {
		return this;
	}
}