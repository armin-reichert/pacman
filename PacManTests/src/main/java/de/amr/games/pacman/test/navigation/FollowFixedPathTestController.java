package de.amr.games.pacman.test.navigation;

import java.util.Arrays;
import java.util.List;

import de.amr.easy.game.view.View;
import de.amr.easy.game.view.ViewController;
import de.amr.games.pacman.actor.GhostState;
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
		g.setLevel(1);
		g.maze.removeFood();
		targets = Arrays.asList(g.maze.getBottomRightCorner(), g.maze.getBottomLeftCorner(),
				g.maze.getTeleportLeft(), g.maze.getTopLeftCorner(), g.maze.getBlinkyHome(),
				g.maze.getTopRightCorner(), g.maze.getTeleportRight(), g.maze.getPacManHome());
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
		g.blinky.initialize();
		g.blinky.setState(GhostState.CHASING);
		g.theme.snd_ghost_chase().stop();
		g.blinky.setBehavior(GhostState.CHASING,
				g.blinky.followingFixedPath(() -> targets.get(targetIndex)));
		g.blinky.currentBehavior().computePath(g.blinky);
	}

	private void nextTarget() {
		targetIndex += 1;
		if (targetIndex == targets.size()) {
			targetIndex = 0;
			g.setLevel(g.getLevel() + 1);
		}
	}

	@Override
	public void update() {
		g.blinky.update();
		if (g.blinky.tilePosition().equals(targets.get(targetIndex))) {
			nextTarget();
		}
		view.update();
	}

	@Override
	public View currentView() {
		return view;
	}
}