package de.amr.games.pacman.test.navigation;

import de.amr.easy.game.Application;
import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.view.View;
import de.amr.easy.game.view.VisualController;
import de.amr.games.pacman.actor.Cast;
import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.actor.GhostState;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.view.play.PlayViewX;

public class ScatteringTestController implements VisualController {

	private final Game game;
	private final PlayViewX view;
	private final Cast actors;

	public ScatteringTestController(int width, int height) {
		Maze maze = new Maze(Assets.text("maze.txt"));
		game = new Game(maze, Application.PULSE::getFrequency);
		actors = new Cast(game);
		view = new PlayViewX(width, height, game);
		view.setActors(actors);
		view.showRoutes = true;
		view.showStates = true;
		view.setScoresVisible(false);
	}

	@Override
	public void init() {
		game.init();
		actors.pacMan.initPacMan();
		actors.pacMan.setEventsEnabled(false);
		actors.getActiveGhosts().forEach(ghost -> {
			ghost.initGhost();
			ghost.setState(GhostState.SCATTERING);
		});
		Application.PULSE.setFrequency(60);
	}

	@Override
	public void update() {
		actors.getActiveGhosts().forEach(Ghost::update);
		view.update();
	}

	@Override
	public View currentView() {
		return view;
	}
}