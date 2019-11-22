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

	private final PacManGame g;
	private final PlayViewXtended view;
	private List<Tile> targets;
	private int targetIndex;

	public FollowTargetTilesTestController() {
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
		targets = Arrays.asList(g.maze.topLeft, g.maze.blinkyHome, g.maze.topRight, g.maze.bottomRight,
				g.maze.pacManHome, g.maze.bottomLeft);
		targetIndex = 0;
		g.setActive(g.pacMan, false);
		g.ghosts().filter(ghost -> ghost != g.blinky).forEach(ghost -> g.setActive(ghost, false));
		g.blinky.init();
		g.blinky.placeAtTile(targets.get(0), 0, 0);
		g.blinky.targetTile = targets.get(0);
		g.blinky.visualizePath = true;
		g.blinky.setState(CHASING);
		g.blinky.setBehavior(CHASING, g.blinky.headingFor(() -> g.blinky.targetTile));
	}

	private void nextTarget() {
		targetIndex += 1;
		if (targetIndex == targets.size()) {
			targetIndex = 0;
			g.setLevel(g.getLevel() + 1);
		}
		g.blinky.targetTile = targets.get(targetIndex);
	}

	@Override
	public void update() {
		g.blinky.update();
		if (g.blinky.currentTile().equals(targets.get(targetIndex))) {
			nextTarget();
		}
		view.update();
	}

	@Override
	public View currentView() {
		return view;
	}
}