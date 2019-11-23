package de.amr.games.pacman.test.navigation;

import de.amr.easy.game.view.View;
import de.amr.easy.game.view.ViewController;
import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.actor.GhostState;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.view.play.PlayViewXtended;

public class InkyChaseTestController implements ViewController {

	private final PacManGame g;
	private final PlayViewXtended view;

	public InkyChaseTestController() {
		g = new PacManGame();
		g.level = 1;
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
		g.setActive(g.pinky, false);
		g.setActive(g.clyde, false);
		g.activeGhosts().forEach(ghost -> {
			ghost.init();
			ghost.fnIsUnlocked = g -> true;
			ghost.fnNextState = () -> GhostState.CHASING;
		});
	}

	@Override
	public void update() {
		g.pacMan.update();
		g.activeGhosts().forEach(Ghost::update);
		view.update();
	}

	@Override
	public View currentView() {
		return view;
	}
}