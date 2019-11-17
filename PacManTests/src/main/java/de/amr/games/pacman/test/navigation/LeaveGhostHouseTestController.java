package de.amr.games.pacman.test.navigation;

import java.awt.event.KeyEvent;

import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.view.View;
import de.amr.easy.game.view.ViewController;
import de.amr.games.pacman.actor.GhostState;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.view.play.PlayViewXtended;

public class LeaveGhostHouseTestController implements ViewController {

	private final PacManGame g;
	private final PlayViewXtended view;

	public LeaveGhostHouseTestController() {
		g = new PacManGame();
		g.setLevel(1);
		g.maze.removeFood();
		view = new PlayViewXtended(g);
		view.setShowRoutes(true);
		view.setShowGrid(true);
		view.setShowStates(true);
		view.setScoresVisible(false);
	}

	@Override
	public void init() {
		g.pacMan.hide();
		g.ghosts().filter(ghost -> ghost != g.inky).forEach(ghost -> g.setActive(ghost, false));
		g.inky.init();
		g.inky.visualizeCompletePath = true;
		g.inky.fnNextState = () -> GhostState.SCATTERING;
		g.inky.setState(GhostState.SCATTERING);
	}

	@Override
	public void update() {
		g.inky.update();
		if (g.inky.getState() == GhostState.LOCKED && Keyboard.keyPressedOnce(KeyEvent.VK_X)) {
			g.inky.setState(GhostState.SCATTERING);
		}
		view.update();
	}

	@Override
	public View currentView() {
		return view;
	}
}