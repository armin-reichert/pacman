package de.amr.games.pacman.test.navigation;

import java.awt.Color;
import java.awt.event.KeyEvent;

import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.view.View;
import de.amr.easy.game.view.ViewController;
import de.amr.games.pacman.actor.GhostState;
import de.amr.games.pacman.controller.event.GhostUnlockedEvent;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.view.play.PlayViewXtended;

public class LeaveGhostHouseTestUI extends PlayViewXtended implements ViewController {

	public LeaveGhostHouseTestUI(PacManGame game) {
		super(game);
		showRoutes = true;
		showGrid = true;
		showStates = true;
		showScores = false;
	}

	@Override
	public void init() {
		super.init();
		game.level = 1;
		game.maze.removeFood();
		game.setActive(game.pacMan, false);
		game.setActive(game.inky, true);
		game.inky.init();
		game.inky.fnNextState = () -> GhostState.SCATTERING;
		showInfoText("Press SPACE to unlock", Color.YELLOW);
	}

	@Override
	public void update() {
		if (Keyboard.keyPressedOnce(KeyEvent.VK_SPACE)) {
			game.inky.process(new GhostUnlockedEvent());
			hideInfoText();
		}
		game.inky.update();
		super.update();
	}

	@Override
	public View currentView() {
		return this;
	}
}