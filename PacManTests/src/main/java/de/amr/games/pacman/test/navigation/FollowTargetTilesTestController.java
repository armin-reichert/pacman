package de.amr.games.pacman.test.navigation;

import static de.amr.games.pacman.actor.GhostState.CHASING;

import java.util.Arrays;
import java.util.List;

import de.amr.easy.game.view.View;
import de.amr.easy.game.view.ViewController;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.view.play.PlayViewXtended;

public class FollowTargetTilesTestController implements ViewController {

	private PacManGame g;
	private PlayViewXtended view;
	private List<Tile> targets;
	private int current;

	@Override
	public void init() {
		g = new PacManGame();
		g.level = 1;
		g.maze.removeFood();
		
		targets = Arrays.asList(g.maze.topLeft, g.maze.blinkyHome, g.maze.topRight, g.maze.bottomRight,
				g.maze.pacManHome, g.maze.bottomLeft);
		current = 0;

		g.setActive(g.pacMan, false);
		g.ghosts().filter(ghost -> ghost != g.blinky).forEach(ghost -> g.setActive(ghost, false));
		g.blinky.init();
		g.blinky.fnChasingTarget = () -> targets.get(current);
		g.blinky.placeAtTile(targets.get(0), 0, 0);
		g.blinky.visualizePath = true;
		g.blinky.setState(CHASING);
		
		view = new PlayViewXtended(g);
		view.setShowRoutes(true);
		view.setShowGrid(false);
		view.setShowStates(false);
		view.setScoresVisible(false);
	}

	private void nextTarget() {
		current += 1;
		if (current == targets.size()) {
			current = 0;
			g.level += 1;
		}
	}

	@Override
	public void update() {
		if (g.blinky.currentTile() == targets.get(current)) {
			nextTarget();
		}
		g.blinky.update();
		view.update();
	}

	@Override
	public View currentView() {
		return view;
	}
}