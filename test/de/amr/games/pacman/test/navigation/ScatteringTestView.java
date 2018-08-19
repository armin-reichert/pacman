package de.amr.games.pacman.test.navigation;

import de.amr.easy.game.Application;
import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.view.Controller;
import de.amr.easy.game.view.View;
import de.amr.games.pacman.actor.game.Cast;
import de.amr.games.pacman.actor.game.GhostState;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.navigation.impl.NavigationSystem;
import de.amr.games.pacman.view.ExtendedGamePanel;

public class ScatteringTestView implements Controller {

	private final Game game;
	private final ExtendedGamePanel gamePanel;
	private final Cast actors;

	public ScatteringTestView(int width, int height) {
		Maze maze = new Maze(Assets.text("maze.txt"));
		game = new Game(maze, Application.PULSE::getFrequency);
		actors = new Cast(game);
		gamePanel = new ExtendedGamePanel(width, height, game, actors);
	}

	@Override
	public void init() {
		game.init();
		Tile scatteringTarget = new Tile(2, 0);
		actors.getPinky().setNavigation(GhostState.SCATTERING, NavigationSystem.followTargetTile(() -> scatteringTarget));
		actors.getPinky().init();
		actors.getPinky().setState(GhostState.SCATTERING);
	}

	@Override
	public void update() {
		gamePanel.update();
		actors.getPinky().update();
	}

	@Override
	public View currentView() {
		return gamePanel;
	}
}
