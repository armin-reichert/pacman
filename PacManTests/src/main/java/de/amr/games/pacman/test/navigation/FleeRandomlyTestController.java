package de.amr.games.pacman.test.navigation;

import static de.amr.games.pacman.actor.GhostState.FRIGHTENED;

import de.amr.easy.game.view.View;
import de.amr.easy.game.view.ViewController;
import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.view.play.PlayViewXtended;

public class FleeRandomlyTestController implements ViewController {

	private PacManGame g;
	private PlayViewXtended view;

	@Override
	public void init() {
		g = new PacManGame();
		g.level = 1;
		g.maze.removeFood();
		g.setActive(g.pacMan, false);
		g.ghosts().forEach(ghost -> {
			ghost.init();
			ghost.setState(FRIGHTENED);
		});
		g.blinky.placeAtTile(g.maze.topLeft, 0, 0);
		g.pinky.placeAtTile(g.maze.topRight, 0, 0);
		g.inky.placeAtTile(g.maze.bottomLeft, 0, 0);
		g.clyde.placeAtTile(g.maze.bottomRight, 0, 0);
		view = new PlayViewXtended(g);
		view.setShowRoutes(true);
		view.setShowGrid(false);
		view.setShowStates(true);
		view.setScoresVisible(false);
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