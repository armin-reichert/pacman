package de.amr.games.pacman.test.navigation;

import java.util.Arrays;
import java.util.List;

import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.view.View;
import de.amr.easy.game.view.ViewController;
import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.actor.GhostState;
import de.amr.games.pacman.actor.PacMan;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.view.play.PlayViewX;

public class FollowFixedPathTestController implements ViewController {

	private final PacManGame game;
	private final PacMan pacMan;
	private final Ghost blinky;
	private final PlayViewX view;
	private final List<Tile> targets;
	private int targetIndex;

	public FollowFixedPathTestController() {
		Maze maze = new Maze(Assets.text("maze.txt"));
		game = new PacManGame(maze);
		pacMan = game.getPacMan();
		blinky = game.getBlinky();
		targets = Arrays.asList(maze.getBottomRightCorner(), maze.getBottomLeftCorner(),
				maze.getLeftTunnelEntry(), maze.getTopLeftCorner(), maze.getBlinkyHome(), maze.getTopRightCorner(),
				maze.getRightTunnelEntry(), maze.getPacManHome());
		view = new PlayViewX(game);
		view.setShowRoutes(true);
		view.setShowGrid(false);
		view.setShowStates(false);
		view.setScoresVisible(false);
	}

	@Override
	public void init() {
		targetIndex = 0;
		game.setLevel(1);
		game.getMaze().tiles().filter(game.getMaze()::isFood).forEach(game::eatFoodAtTile);
		game.setActive(pacMan, false);
		game.getGhosts().filter(ghost -> ghost != blinky).forEach(ghost -> game.setActive(ghost, false));
		blinky.initGhost();
		blinky.setState(GhostState.CHASING);
		blinky.setMoveBehavior(GhostState.CHASING, blinky.followFixedPath(() -> targets.get(targetIndex)));
		blinky.getMoveBehavior().computePath(blinky);
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
		blinky.update();
		if (blinky.getTile().equals(targets.get(targetIndex))) {
			nextTarget();
		}
		view.update();
	}

	@Override
	public View currentView() {
		return view;
	}
}