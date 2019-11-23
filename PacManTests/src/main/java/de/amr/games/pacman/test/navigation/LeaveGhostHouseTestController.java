package de.amr.games.pacman.test.navigation;

import de.amr.easy.game.Application;
import de.amr.easy.game.view.View;
import de.amr.easy.game.view.ViewController;
import de.amr.games.pacman.actor.GhostState;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.view.play.PlayViewXtended;

public class LeaveGhostHouseTestController implements ViewController {

	private PacManGame g;
	private PlayViewXtended view;

	@Override
	public void init() {
		g = new PacManGame();
		g.level = 1;
		g.maze.removeFood();
		g.pacMan.hide();
		g.ghosts().filter(ghost -> ghost != g.inky).forEach(ghost -> g.setActive(ghost, false));
		g.inky.init();
		g.inky.visualizePath = true;
		g.inky.fnIsUnlocked = g -> true;
		g.inky.fnNextState = () -> GhostState.SCATTERING;
		view = new PlayViewXtended(g);
		view.setShowRoutes(true);
		view.setShowGrid(true);
		view.setShowStates(true);
		view.setScoresVisible(false);
		Application.app().clock.setFrequency(10);
	}

	@Override
	public void update() {
		g.inky.update();
		view.update();
	}

	@Override
	public View currentView() {
		return view;
	}
}