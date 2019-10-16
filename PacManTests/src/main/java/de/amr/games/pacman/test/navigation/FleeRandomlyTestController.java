package de.amr.games.pacman.test.navigation;

import de.amr.easy.game.view.View;
import de.amr.easy.game.view.ViewController;
import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.actor.GhostState;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.view.play.PlayViewXtended;

public class FleeRandomlyTestController implements ViewController {

	private final PacManGame game;
	private final PlayViewXtended view;

	public FleeRandomlyTestController() {
		game = new PacManGame();
		game.setLevel(1);
		game.maze.removeFood();
		view = new PlayViewXtended(game);
		view.setShowRoutes(true);
		view.setShowGrid(false);
		view.setShowStates(false);
		view.setScoresVisible(false);
	}

	@Override
	public void init() {
		game.setActive(game.pacMan, false);
		game.ghosts().forEach(ghost -> {
			game.setActive(ghost, true);
			ghost.initialize();
			ghost.setBehavior(GhostState.FRIGHTENED, ghost.fleeRandomly());
			ghost.setState(GhostState.FRIGHTENED);
		});
	}

	@Override
	public void update() {
		game.activeGhosts().forEach(Ghost::update);
		view.update();
	}

	@Override
	public View currentView() {
		return view;
	}
}