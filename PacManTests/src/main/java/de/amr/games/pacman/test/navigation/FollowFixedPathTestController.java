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

	private final PacManGame game;
	private final PlayViewXtended view;
	private final List<Tile> targets;
	private int targetIndex;

	public FollowFixedPathTestController() {
		game = new PacManGame();
		game.setLevel(1);
		game.maze.removeFood();
		targets = Arrays.asList(game.maze.getBottomRightCorner(), game.maze.getBottomLeftCorner(),
				game.maze.getLeftTunnelEntry(), game.maze.getTopLeftCorner(), game.maze.getBlinkyHome(),
				game.maze.getTopRightCorner(), game.maze.getRightTunnelEntry(), game.maze.getPacManHome());
		view = new PlayViewXtended(game);
		view.setShowRoutes(true);
		view.setShowGrid(false);
		view.setShowStates(false);
		view.setScoresVisible(false);
	}

	@Override
	public void init() {
		targetIndex = 0;
		game.setActive(game.pacMan, false);
		game.ghosts().filter(ghost -> ghost != game.blinky)
				.forEach(ghost -> game.setActive(ghost, false));
		game.blinky.initialize();
		game.blinky.setState(GhostState.CHASING);
		game.blinky.setBehavior(GhostState.CHASING,
				game.blinky.followingFixedPath(() -> targets.get(targetIndex)));
		game.blinky.currentBehavior().computePath(game.blinky);
	}

	private void nextTarget() {
		targetIndex += 1;
		if (targetIndex == targets.size()) {
			targetIndex = 0;
			game.setLevel(game.getLevel() + 1);
		}
	}

	@Override
	public void update() {
		game.blinky.update();
		if (game.blinky.getTile().equals(targets.get(targetIndex))) {
			nextTarget();
		}
		view.update();
	}

	@Override
	public View currentView() {
		return view;
	}
}