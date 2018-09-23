package de.amr.games.pacman.test.navigation;

import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.view.View;
import de.amr.easy.game.view.ViewController;
import de.amr.games.pacman.actor.PacManActors;
import de.amr.games.pacman.actor.GhostState;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.view.play.PlayViewX;

public class EscapeIntoCornerTestController implements ViewController {

	private final PacManGame game;
	private final PlayViewX view;
	private final PacManActors actors;

	public EscapeIntoCornerTestController() {
		Maze maze = new Maze(Assets.text("maze.txt"));
		game = new PacManGame(maze);
		actors = new PacManActors(game);
		view = new PlayViewX(game, actors);
		view.setShowRoutes(true);
		view.setShowGrid(false);
		view.setShowStates(true);
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