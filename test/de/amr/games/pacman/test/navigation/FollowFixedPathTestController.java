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
	private int currentIndex;

	public FollowFixedPathTestController(int width, int height) {
		Maze maze = new Maze(Assets.text("maze.txt"));
		game = new Game(maze, Application.PULSE::getFrequency);
		actors = new Cast(game);
		targets = Arrays.asList(maze.getBottomRightCorner(), maze.getBottomLeftCorner(),
				maze.getTopLeftCorner(), maze.getTopRightCorner());
		view = new PlayViewX(width, height, game);
		view.setActors(actors);
		view.showRoutes = true;
		view.showGrid = false;
		view.showStates = true;
		view.setScoresVisible(true);
	}

	@Override
	public void init() {
		Application.PULSE.setFrequency(60);
		game.init();
		game.getMaze().tiles().filter(game.getMaze()::isFood).forEach(game::eatFoodAtTile);
		actors.getPacMan().initPacMan();
		actors.getPacMan().setEventsEnabled(false);
		actors.getBlinky().initGhost();
		actors.getBlinky().setState(GhostState.AGGRO);
		currentIndex = 0;
		actors.getBlinky().setMoveBehavior(GhostState.AGGRO,
				actors.getBlinky().followStaticRoute(targets.get(0)));
		actors.getBlinky().getMoveBehavior().computeStaticRoute(actors.getBlinky());
	}

	private void nextTarget() {
		currentIndex += 1;
		if (currentIndex == targets.size()) {
			currentIndex = 0;
			game.setLevel(game.getLevel() + 1);
		}
		actors.getBlinky().setMoveBehavior(GhostState.AGGRO,
				actors.getBlinky().followStaticRoute(targets.get(currentIndex)));
		actors.getBlinky().getMoveBehavior().computeStaticRoute(actors.getBlinky());
	}

	@Override
	public void update() {
		actors.getBlinky().update();
		if (actors.getBlinky().getTile().equals(targets.get(currentIndex))) {
			nextTarget();
		}
		view.update();
	}

	@Override
	public View currentView() {
		return view;
	}
}