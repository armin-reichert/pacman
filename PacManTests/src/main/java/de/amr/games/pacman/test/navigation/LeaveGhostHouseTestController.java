package de.amr.games.pacman.test.navigation;

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
		g.level = 1;
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
		g.inky.visualizePath = true;
		g.inky.fnIsUnlocked = g -> true;
		g.inky.fnNextState = () -> GhostState.SCATTERING;
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