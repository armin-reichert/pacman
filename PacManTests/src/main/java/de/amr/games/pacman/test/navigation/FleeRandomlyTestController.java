package de.amr.games.pacman.test.navigation;

import static de.amr.games.pacman.actor.GhostState.FRIGHTENED;

import de.amr.easy.game.view.View;
import de.amr.easy.game.view.ViewController;
import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.view.play.PlayViewXtended;

public class FleeRandomlyTestController implements ViewController {

	private final PacManGame g;
	private final PlayViewXtended view;

	public FleeRandomlyTestController() {
		g = new PacManGame();
		g.setLevel(1);
		g.maze.removeFood();
		view = new PlayViewXtended(g);
		view.setShowRoutes(true);
		view.setShowGrid(false);
		view.setShowStates(false);
		view.setScoresVisible(false);
	}

	@Override
	public void init() {
		g.setActive(g.pacMan, false);
		g.ghosts().forEach(ghost -> {
			g.setActive(ghost, true);
			ghost.initialize();
			ghost.setBehavior(FRIGHTENED, ghost.fleeingRandomly());
			ghost.setState(FRIGHTENED);
		});
	}

	@Override
	public void update() {
		g.activeGhosts().forEach(Ghost::update);
		view.update();
	}

	@Override
	public View currentView() {
		return view;
	}
}