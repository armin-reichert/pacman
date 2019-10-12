package de.amr.games.pacman.test.navigation;

import java.awt.event.KeyEvent;

import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.view.View;
import de.amr.easy.game.view.ViewController;
import de.amr.games.pacman.actor.GhostState;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.view.play.PlayViewXtended;

public class LeaveGhostHouseTestController implements ViewController {

	private final PacManGame game;
	private final PlayViewXtended view;

	public LeaveGhostHouseTestController() {
		game = new PacManGame();
		game.setLevel(1);
		game.maze.removeFood();
		view = new PlayViewXtended(game);
		view.setShowRoutes(true);
		view.setShowGrid(true);
		view.setShowStates(true);
		view.setScoresVisible(false);
	}

	@Override
	public void init() {
		game.pacMan.setVisible(false);
		game.ghosts().filter(ghost -> ghost != game.inky).forEach(ghost -> game.setActive(ghost, false));
		game.inky.initGhost();
		game.inky.fnNextState = () -> GhostState.SCATTERING;
		game.inky.setState(GhostState.SCATTERING);
	}

	@Override
	public void update() {
		game.inky.update();
		if (game.inky.getState() == GhostState.LOCKED && Keyboard.keyPressedOnce(KeyEvent.VK_X)) {
			game.inky.setState(GhostState.SCATTERING);
		}
		view.update();
	}

	@Override
	public View currentView() {
		return view;
	}
}