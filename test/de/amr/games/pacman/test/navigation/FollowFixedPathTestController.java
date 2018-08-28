package de.amr.games.pacman.test.navigation;

import java.util.Arrays;
import java.util.List;

import de.amr.easy.game.Application;
import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.view.Controller;
import de.amr.easy.game.view.View;
import de.amr.games.pacman.actor.Cast;
import de.amr.games.pacman.actor.GhostState;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.view.play.PlayViewX;

public class FollowFixedPathTestController implements Controller {

	private final Game game;
	private final PlayViewX view;
	private final Cast actors;
	private final List<Tile> targets;
	private int targetIndex;

	public FollowFixedPathTestController(int width, int height) {
		Maze maze = new Maze(Assets.text("maze.txt"));
		game = new Game(maze, Application.PULSE::getFrequency);
		actors = new Cast(game);
		targets = Arrays.asList(maze.getBottomRightCorner(), maze.getBottomLeftCorner(),
				maze.getLeftTunnelEntry(), maze.getTopLeftCorner(), maze.getBlinkyHome(),
				maze.getTopRightCorner(), maze.getRightTunnelEntry(), maze.getPacManHome());
		view = new PlayViewX(width, height, game);
		view.setActors(actors);
		view.showRoutes = true;
		view.showGrid = false;
		view.showStates = true;
		view.setScoresVisible(false);
	}

	@Override
	public void init() {
		targetIndex = 0;
		game.setLevel(1);
		game.getMaze().tiles().filter(game.getMaze()::isFood).forEach(game::eatFoodAtTile);
		actors.setActive(actors.getPacMan(), false);
		actors.getBlinky().initGhost();
		actors.getBlinky().setState(GhostState.CHASING);
		actors.getBlinky().setMoveBehavior(GhostState.CHASING,
				actors.getBlinky().followStaticRoute(targets.get(0)));
		actors.getBlinky().getMoveBehavior().computeStaticRoute(actors.getBlinky());
	}

	private void nextTarget() {
		targetIndex += 1;
		if (targetIndex == targets.size()) {
			targetIndex = 0;
			game.setLevel(game.getLevel() + 1);
		}
		actors.getBlinky().setMoveBehavior(GhostState.CHASING,
				actors.getBlinky().followStaticRoute(targets.get(targetIndex)));
		actors.getBlinky().getMoveBehavior().computeStaticRoute(actors.getBlinky());
	}

	@Override
	public void update() {
		actors.getBlinky().update();
		if (actors.getBlinky().getTile().equals(targets.get(targetIndex))) {
			nextTarget();
		}
		view.update();
	}

	@Override
	public View currentView() {
		return view;
	}
}