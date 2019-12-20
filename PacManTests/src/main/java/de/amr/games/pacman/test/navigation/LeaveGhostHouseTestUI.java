package de.amr.games.pacman.test.navigation;

import java.awt.Color;
import java.awt.event.KeyEvent;

import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.view.View;
import de.amr.easy.game.view.VisualController;
import de.amr.games.pacman.actor.GhostState;
import de.amr.games.pacman.actor.PacManGameCast;
import de.amr.games.pacman.controller.event.GhostUnlockedEvent;
import de.amr.games.pacman.view.play.PlayView;

public class LeaveGhostHouseTestUI extends PlayView implements VisualController {

	public LeaveGhostHouseTestUI(PacManGameCast cast) {
		super(cast);
		setShowRoutes(true);
		setShowStates(true);
		setShowScores(false);
		setShowGrid(true);
	}

	@Override
	public void init() {
		super.init();
		game().init();
		maze().removeFood();
		cast().putOnStage(cast().inky);
		cast().inky.nextState = GhostState.SCATTERING;
		textColor = Color.YELLOW;
		message = "Press SPACE to unlock";
	}

	@Override
	public void update() {
		if (Keyboard.keyPressedOnce(KeyEvent.VK_SPACE)) {
			cast().inky.process(new GhostUnlockedEvent());
			message = null;
		}
		cast().inky.update();
		super.update();
	}

	@Override
	public View currentView() {
		return this;
	}
}