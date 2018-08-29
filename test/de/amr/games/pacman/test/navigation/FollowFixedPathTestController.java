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
		actors.setActive(actors.pacMan, false);
		actors.getGhosts().filter(ghost -> ghost != actors.blinky)
				.forEach(ghost -> actors.setActive(ghost, false));
		actors.blinky.initGhost();
		actors.blinky.setState(GhostState.CHASING);
		actors.blinky.setMoveBehavior(GhostState.CHASING,
				actors.blinky.followStaticRoute(targets.get(0)));
		actors.blinky.getMoveBehavior().computeStaticRoute(actors.blinky);
	}

	private void nextTarget() {
		targetIndex += 1;
		if (targetIndex == targets.size()) {
			targetIndex = 0;
			game.setLevel(game.getLevel() + 1);
		}
		actors.blinky.setMoveBehavior(GhostState.CHASING,
				actors.blinky.followStaticRoute(targets.get(targetIndex)));
		actors.blinky.getMoveBehavior().computeStaticRoute(actors.blinky);
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