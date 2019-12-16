package de.amr.games.pacman.test.navigation;

import static de.amr.games.pacman.actor.behavior.Steerings.avoidingGhosts;
import static de.amr.games.pacman.actor.behavior.Steerings.movingRandomlyNoReversing;
import static de.amr.games.pacman.actor.behavior.Steerings.steeredByKeys;

import java.awt.event.KeyEvent;

import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.view.View;
import de.amr.easy.game.view.VisualController;
import de.amr.games.pacman.actor.PacManGameCast;
import de.amr.games.pacman.actor.core.MazeResident;
import de.amr.games.pacman.controller.event.FoodFoundEvent;
import de.amr.games.pacman.view.play.PlayView;

public class PacManMovementTestUI extends PlayView implements VisualController {

	public PacManMovementTestUI(PacManGameCast cast) {
		super(cast);
		showScores = false;
	}

	@Override
	public void init() {
		super.init();
		game.init();
		cast.pacMan.addGameEventListener(event -> {
			if (event.getClass() == FoodFoundEvent.class) {
				FoodFoundEvent foodFound = (FoodFoundEvent) event;
				cast.theme.snd_eatPill().play();
				foodFound.tile.removeFood();
				game.level.numPelletsEaten++;
				if (game.numPelletsRemaining() == 0) {
					game.maze.restoreFood();
					game.level.numPelletsEaten = 0;
				}
			}
		});
		cast.putOnStage(cast.pacMan);
	}

	@Override
	public void update() {
		handleSteeringChange();
		cast.actorsOnStage().forEach(MazeResident::update);
		super.update();
	}

	private void handleSteeringChange() {
		if (Keyboard.keyPressedOnce(KeyEvent.VK_M)) {
			cast.pacMan.setSteering(steeredByKeys(KeyEvent.VK_UP, KeyEvent.VK_RIGHT, KeyEvent.VK_DOWN, KeyEvent.VK_LEFT));
		} else if (Keyboard.keyPressedOnce(KeyEvent.VK_N)) {
			cast.pacMan.setSteering(
					steeredByKeys(KeyEvent.VK_NUMPAD8, KeyEvent.VK_NUMPAD6, KeyEvent.VK_NUMPAD2, KeyEvent.VK_NUMPAD4));
		} else if (Keyboard.keyPressedOnce(KeyEvent.VK_A)) {
			cast.pacMan.setSteering(avoidingGhosts());
		} else if (Keyboard.keyPressedOnce(KeyEvent.VK_R)) {
			cast.pacMan.setSteering(movingRandomlyNoReversing());
		}
	}

	@Override
	public View currentView() {
		return this;
	}
}