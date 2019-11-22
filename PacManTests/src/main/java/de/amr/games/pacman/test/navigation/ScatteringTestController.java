package de.amr.games.pacman.test.navigation;

import java.awt.Color;
import java.awt.event.KeyEvent;

import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.view.View;
import de.amr.easy.game.view.ViewController;
import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.actor.GhostState;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.view.play.PlayViewXtended;

public class ScatteringTestController implements ViewController {

	private final PacManGame game;
	private final PlayViewXtended view;
	private boolean unlocked = false;

	public ScatteringTestController() {
		game = new PacManGame();
		view = new PlayViewXtended(game);
		view.setShowGrid(false);
		view.setShowRoutes(true);
		view.setShowStates(true);
		view.setScoresVisible(false);
	}

	@Override
	public void init() {
		game.init();
		game.maze.removeFood();
		game.pacMan.hide();
		game.activeGhosts().forEach(ghost -> {
			ghost.init();
			ghost.fnNextState = () -> GhostState.SCATTERING;
			ghost.fnIsUnlocked = g -> unlocked;
			ghost.visualizePath = true;
		});
		view.showInfoText("Press SPACE key", Color.YELLOW);
	}

	@Override
	public void update() {
		if (Keyboard.keyPressedOnce(KeyEvent.VK_SPACE)) {
			unlocked = true;
			view.hideInfoText();
		}
		game.activeGhosts().forEach(Ghost::update);
		view.update();
	}

	@Override
	public View currentView() {
		return view;
	}
}