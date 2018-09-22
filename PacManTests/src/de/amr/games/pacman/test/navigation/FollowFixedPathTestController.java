package de.amr.games.pacman.test.navigation;

import java.util.Arrays;
import java.util.List;

import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.view.View;
import de.amr.easy.game.view.ViewController;
import de.amr.games.pacman.actor.Cast;
import de.amr.games.pacman.actor.GhostState;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.view.play.PlayViewX;

public class FollowFixedPathTestController implements ViewController {

	private final Game game;
	private final PlayViewX view;
	private final Cast actors;
	private final List<Tile> targets;
	private int targetIndex;

	public FollowFixedPathTestController() {
		Maze maze = new Maze(Assets.text("maze.txt"));
		game = new Game(maze);
		actors = new Cast(game);
		targets = Arrays.asList(maze.getBottomRightCorner(), maze.getBottomLeftCorner(),
				maze.getLeftTunnelEntry(), maze.getTopLeftCorner(), maze.getBlinkyHome(), maze.getTopRightCorner(),
				maze.getRightTunnelEntry(), maze.getPacManHome());
		view = new PlayViewX(game, actors);
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
		actors.setActive(actors.pacMan, false);
		actors.getGhosts().filter(ghost -> ghost != actors.blinky)
				.forEach(ghost -> actors.setActive(ghost, false));
		actors.blinky.initGhost();
		actors.blinky.setState(GhostState.CHASING);
		actors.blinky.setMoveBehavior(GhostState.CHASING,
				actors.blinky.followFixedPath(() -> targets.get(targetIndex)));
		actors.blinky.getMoveBehavior().computePath(actors.blinky);
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
		actors.blinky.update();
		if (actors.blinky.getTile().equals(targets.get(targetIndex))) {
			nextTarget();
		}
		view.update();
	}

	@Override
	public View currentView() {
		return view;
	}
}