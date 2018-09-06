package de.amr.games.pacman.test.navigation;

import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.view.View;
import de.amr.easy.game.view.ViewController;
import de.amr.games.pacman.actor.Cast;
import de.amr.games.pacman.actor.GhostState;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.view.play.PlayViewX;

public class EscapeIntoCornerTestController implements ViewController {

	private final Game game;
	private final PlayViewX view;
	private final Cast actors;

	public EscapeIntoCornerTestController() {
		Maze maze = new Maze(Assets.text("maze.txt"));
		game = new Game(maze);
		actors = new Cast(game);
		view = new PlayViewX(game);
		view.setActors(actors);
		view.showRoutes = true;
		view.showGrid = false;
		view.showStates = true;
		view.setScoresVisible(false);
	}

	@Override
	public void init() {
		game.setLevel(1);
		game.getMaze().tiles().filter(game.getMaze()::isFood).forEach(game::eatFoodAtTile);
		actors.setActive(actors.pacMan, true);
		actors.pacMan.init();
		actors.getGhosts().filter(ghost -> ghost != actors.blinky)
				.forEach(ghost -> actors.setActive(ghost, false));
		actors.blinky.initGhost();
		actors.blinky.setState(GhostState.FRIGHTENED);
	}

	@Override
	public void update() {
		actors.pacMan.update();
		actors.blinky.update();
		view.update();
	}

	@Override
	public View currentView() {
		return view;
	}
}