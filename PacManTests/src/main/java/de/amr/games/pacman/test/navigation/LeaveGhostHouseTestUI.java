package de.amr.games.pacman.test.navigation;

import java.awt.Color;
import java.awt.event.KeyEvent;

import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.view.View;
import de.amr.easy.game.view.ViewController;
import de.amr.games.pacman.actor.Ensemble;
import de.amr.games.pacman.actor.GhostState;
import de.amr.games.pacman.controller.event.GhostUnlockedEvent;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.view.play.PlayView;

public class LeaveGhostHouseTestUI extends PlayView implements ViewController {

	public LeaveGhostHouseTestUI(PacManGame game, Ensemble ensemble) {
		super(game, ensemble);
		showRoutes = true;
		showGrid = true;
		showStates = true;
		showScores = false;
	}

	@Override
	public void init() {
		super.init();
		game.levelNumber = 1;
		game.maze.removeFood();
		ensemble.inky.activate();
		ensemble.inky.init();
		ensemble.inky.fnNextState = () -> GhostState.SCATTERING;
		showInfoText("Press SPACE to unlock", Color.YELLOW);
	}

	@Override
	public void update() {
		if (Keyboard.keyPressedOnce(KeyEvent.VK_SPACE)) {
			ensemble.inky.process(new GhostUnlockedEvent());
			hideInfoText();
		}
		ensemble.inky.update();
		super.update();
	}

	@Override
	public View currentView() {
		return this;
	}
}