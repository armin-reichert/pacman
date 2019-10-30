package de.amr.games.pacman.test.navigation;

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

	public ScatteringTestController() {
		game = new PacManGame();
		view = new PlayViewXtended(game);
		view.setShowGrid(true);
		view.setShowRoutes(true);
		view.setShowStates(true);
		view.setScoresVisible(false);
	}

	@Override
	public void init() {
		game.init();
		game.maze.removeFood();
		game.pacMan.setVisible(false);
		game.activeGhosts().forEach(ghost -> {
			ghost.initialize();
			ghost.setState(GhostState.SCATTERING);
		});
	}

	@Override
	public void update() {
		if (Keyboard.keyPressedOnce(KeyEvent.VK_X)) {
			game.activeGhosts().filter(ghost -> ghost.getState() == GhostState.LOCKED).forEach(ghost -> {
				ghost.setState(GhostState.SCATTERING);
			});
		}
		game.activeGhosts().forEach(Ghost::update);
		view.update();
	}

	@Override
	public View currentView() {
		return view;
	}
}