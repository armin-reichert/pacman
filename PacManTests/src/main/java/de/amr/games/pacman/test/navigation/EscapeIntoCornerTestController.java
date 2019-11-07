package de.amr.games.pacman.test.navigation;

import de.amr.easy.game.view.View;
import de.amr.easy.game.view.ViewController;
import de.amr.games.pacman.actor.GhostState;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.view.play.PlayViewXtended;

public class EscapeIntoCornerTestController implements ViewController {

	private final PacManGame g;
	private final PlayViewXtended view;

	public EscapeIntoCornerTestController() {
		g = new PacManGame();
		g.setLevel(1);
		g.maze.removeFood();
		view = new PlayViewXtended(g);
		view.setShowRoutes(true);
		view.setShowGrid(false);
		view.setShowStates(true);
		view.setScoresVisible(false);
	}

	@Override
	public void init() {
		g.pacMan.init();
		g.ghosts().filter(ghost -> ghost != g.blinky).forEach(ghost -> g.setActive(ghost, false));
		g.blinky.setBehavior(GhostState.FRIGHTENED, g.blinky.fleeingToSafeCorner(g.pacMan));
		g.blinky.initialize();
		g.blinky.setState(GhostState.FRIGHTENED);
	}

	@Override
	public void update() {
		g.pacMan.update();
		g.blinky.update();
		view.update();
	}

	@Override
	public View currentView() {
		return view;
	}
}