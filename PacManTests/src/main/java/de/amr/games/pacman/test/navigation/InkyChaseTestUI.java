package de.amr.games.pacman.test.navigation;

import java.awt.Color;
import java.awt.event.KeyEvent;

import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.view.View;
import de.amr.easy.game.view.ViewController;
import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.actor.GhostState;
import de.amr.games.pacman.controller.event.GhostUnlockedEvent;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.view.play.PlayViewXtended;

public class InkyChaseTestUI extends PlayViewXtended implements ViewController {

	public InkyChaseTestUI(PacManGame game) {
		super(game);
		showRoutes = true;
		showStates = true;
		showScores = false;
	}

	@Override
	public void init() {
		super.init();
		game.level = 1;
		game.maze.removeFood();
		game.theme.snd_ghost_chase().volume(0);
		game.pacMan.init();
		game.pacMan.activate(true);
		game.inky.activate(true);
		game.blinky.activate(true);
		game.activeGhosts().forEach(ghost -> {
			ghost.init();
			ghost.fnNextState = () -> GhostState.CHASING;
		});
		showInfoText("Press SPACE to start", Color.YELLOW);
	}

	@Override
	public void update() {
		if (Keyboard.keyPressedOnce(KeyEvent.VK_SPACE)) {
			game.activeGhosts().forEach(ghost -> ghost.process(new GhostUnlockedEvent()));
			hideInfoText();
		}
		game.pacMan.update();
		game.activeGhosts().forEach(Ghost::update);
		super.update();
	}

	@Override
	public View currentView() {
		return this;
	}
}