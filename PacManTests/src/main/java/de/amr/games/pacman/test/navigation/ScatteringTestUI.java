package de.amr.games.pacman.test.navigation;

import java.awt.Color;
import java.awt.event.KeyEvent;

import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.view.View;
import de.amr.easy.game.view.VisualController;
import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.actor.GhostState;
import de.amr.games.pacman.actor.PacManGameCast;
import de.amr.games.pacman.controller.event.GhostUnlockedEvent;
import de.amr.games.pacman.view.play.PlayView;

public class ScatteringTestUI extends PlayView implements VisualController {

	public ScatteringTestUI(PacManGameCast cast) {
		super(cast);
		setShowRoutes(true);
		setShowStates(true);
		setShowScores(false);
		setShowGrid(false);
	}

	@Override
	public void init() {
		super.init();
		game().init();
		maze().removeFood();
		cast().ghosts().forEach(ghost -> {
			cast().putOnStage(ghost);
			ghost.nextState = GhostState.SCATTERING;
		});
		messageColor = Color.YELLOW;
		messageText = "Press SPACE to start";
	}

	@Override
	public void update() {
		if (Keyboard.keyPressedOnce(KeyEvent.VK_SPACE)) {
			cast().ghostsOnStage().forEach(ghost -> ghost.process(new GhostUnlockedEvent()));
			messageText = null;
		}
		cast().ghostsOnStage().forEach(Ghost::update);
		super.update();
	}

	@Override
	public View currentView() {
		return this;
	}
}