package de.amr.games.pacman.test.navigation;

import java.awt.event.KeyEvent;

import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.view.View;
import de.amr.easy.game.view.ViewController;
import de.amr.games.pacman.actor.Actor;
import de.amr.games.pacman.actor.Ensemble;
import de.amr.games.pacman.actor.behavior.pacman.PacManSteerings;
import de.amr.games.pacman.controller.event.FoodFoundEvent;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.theme.ClassicPacManTheme;
import de.amr.games.pacman.view.play.PlayView;

public class PacManMovementTestUI extends PlayView implements ViewController {

	public PacManMovementTestUI(PacManGame game, Ensemble ensemble) {
		super(game, ensemble, new ClassicPacManTheme());
		showScores = false;
	}

	@Override
	public void init() {
		super.init();
		game.levelNumber = 1;
		ensemble.pacMan.addListener(event -> {
			if (event.getClass() == FoodFoundEvent.class) {
				FoodFoundEvent foodFound = (FoodFoundEvent) event;
				theme.snd_eatPill().play();
				game.maze.removeFood(foodFound.tile);
				if (game.maze.tiles().filter(game.maze::containsFood).count() == 0) {
					game.maze.restoreFood();
				}
			}
		});
		ensemble.pacMan.activate();
		ensemble.pacMan.init();
	}

	@Override
	public void update() {
		handleSteeringChange();
		ensemble.activeActors().forEach(Actor::update);
		super.update();
	}

	private void handleSteeringChange() {
		if (Keyboard.keyPressedOnce(KeyEvent.VK_M)) {
			ensemble.pacMan.steering = PacManSteerings.steeredByKeys(KeyEvent.VK_UP, KeyEvent.VK_RIGHT, KeyEvent.VK_DOWN,
					KeyEvent.VK_LEFT);
		} else if (Keyboard.keyPressedOnce(KeyEvent.VK_N)) {
			ensemble.pacMan.steering = PacManSteerings.steeredByKeys(KeyEvent.VK_NUMPAD5, KeyEvent.VK_NUMPAD3,
					KeyEvent.VK_NUMPAD2, KeyEvent.VK_NUMPAD1);
		} else if (Keyboard.keyPressedOnce(KeyEvent.VK_A)) {
			ensemble.pacMan.steering = PacManSteerings.avoidGhosts();
		} else if (Keyboard.keyPressedOnce(KeyEvent.VK_R)) {
			ensemble.pacMan.steering = PacManSteerings.movingRandomly();
		}
	}

	@Override
	public View currentView() {
		return this;
	}
}