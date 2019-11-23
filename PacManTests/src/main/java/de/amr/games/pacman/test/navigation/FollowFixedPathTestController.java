package de.amr.games.pacman.test.navigation;

import static de.amr.games.pacman.actor.GhostState.CHASING;
import static de.amr.games.pacman.actor.GhostState.FRIGHTENED;

import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.List;

import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.view.View;
import de.amr.easy.game.view.ViewController;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.view.play.PlayViewXtended;

public class FollowFixedPathTestController implements ViewController {

	private final PacManGame g;
	private final PlayViewXtended view;
	private final List<Tile> targets;
	private int targetIndex;

	public FollowFixedPathTestController() {
		g = new PacManGame();
		g.level = 1;
		g.maze.removeFood();
		targets = Arrays.asList(g.maze.bottomRight, g.maze.bottomLeft, g.maze.tunnelLeftExit, g.maze.topLeft,
				g.maze.blinkyHome, g.maze.topRight, g.maze.tunnelRightExit, g.maze.pacManHome);
		view = new PlayViewXtended(g);
		view.setShowRoutes(true);
		view.setShowGrid(false);
		view.setShowStates(false);
		view.setScoresVisible(false);
	}

	@Override
	public void init() {
		targetIndex = 0;
		g.setActive(g.pacMan, false);
		g.ghosts().filter(ghost -> ghost != g.blinky).forEach(ghost -> g.setActive(ghost, false));
		g.blinky.init();
		g.blinky.setState(CHASING);
		g.blinky.setBehavior(CHASING, g.blinky.followingFixedPath(() -> targets.get(targetIndex)));
		g.blinky.setBehavior(FRIGHTENED, g.blinky.followingFixedPath(() -> targets.get(targetIndex)));
	}

	private void nextTarget() {
		targetIndex += 1;
		if (targetIndex == targets.size()) {
			targetIndex = 0;
			g.level += 1;
		}
	}

	@Override
	public void update() {
		if (Keyboard.keyPressedOnce(KeyEvent.VK_F)) {
			g.blinky.setState(g.blinky.getState() == CHASING ? FRIGHTENED : CHASING);
		}
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