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

public class InkyChaseTestUI extends PlayView implements VisualController {

	public InkyChaseTestUI(PacManGameCast cast) {
		super(cast);
		showRoutes = true;
		showStates = true;
		showScores = false;
	}

	@Override
	public void init() {
		super.init();
		game.newGame();
		game.maze.removeFood();
		cast.theme.snd_ghost_chase().volume(0);
		cast.pacMan.init();
		cast.pacMan.activate();
		cast.inky.activate();
		cast.blinky.activate();
		cast.activeGhosts().forEach(ghost -> {
			ghost.init();
			ghost.nextState = GhostState.CHASING;
		});
		textColor = Color.YELLOW;
		message = "Press SPACE to start";
	}

	@Override
	public void update() {
		if (Keyboard.keyPressedOnce(KeyEvent.VK_SPACE)) {
			cast.activeGhosts().forEach(ghost -> ghost.process(new GhostUnlockedEvent()));
			message = null;
		}
		cast.pacMan.update();
		cast.activeGhosts().forEach(Ghost::update);
		super.update();
	}

	@Override
	public View currentView() {
		return this;
	}
}