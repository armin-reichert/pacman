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
import de.amr.games.pacman.navigation.NavigationSystem;
import de.amr.games.pacman.view.play.ExtendedGamePanel;

public class FollowFixedPathTestView implements Controller {

	private final Game game;
	private final ExtendedGamePanel gamePanel;
	private final Cast actors;
	private final List<Tile> targets;
	private int currentIndex;

	public FollowFixedPathTestView(int width, int height) {
		Maze maze = new Maze(Assets.text("maze.txt"));
		game = new Game(maze, Application.PULSE::getFrequency);
		actors = new Cast(game);
		targets = Arrays.asList(maze.getBottomRightCorner(), maze.getBottomLeftCorner(), maze.getTopLeftCorner(),
				maze.getBottomRightCorner());
		gamePanel = new ExtendedGamePanel(width, height, game, actors);
		gamePanel.showRoutes = true;
		gamePanel.setScoresVisible(false);
	}

	@Override
	public void init() {
		Application.PULSE.setFrequency(60);
		game.init();
		actors.getPacMan().initPacMan();
		actors.getPacMan().setEventsEnabled(false);
		actors.getBlinky().initGhost();
		actors.getBlinky().setState(GhostState.AGGRO);
		currentIndex = 0;
		actors.getBlinky().setNavigation(GhostState.AGGRO, NavigationSystem.followFixedPath(targets.get(0)));
		actors.getBlinky().getNavigation().computeStaticRoute(actors.getBlinky());
	}
	
	private void nextTarget() {
		currentIndex += 1;
		if (currentIndex == targets.size()) {
			currentIndex = 0;
		}
		actors.getBlinky().setNavigation(GhostState.AGGRO, NavigationSystem.followFixedPath(targets.get(currentIndex)));
		actors.getBlinky().getNavigation().computeStaticRoute(actors.getBlinky());
	}

	@Override
	public void update() {
		gamePanel.update();
		actors.getBlinky().update();
		if (actors.getBlinky().getTile().equals(targets.get(currentIndex))) {
			nextTarget();
		}
	}

	@Override
	public View currentView() {
		return gamePanel;
	}

}
