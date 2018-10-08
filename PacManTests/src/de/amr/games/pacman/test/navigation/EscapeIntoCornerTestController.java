package de.amr.games.pacman.test.navigation;

import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.view.View;
import de.amr.easy.game.view.ViewController;
import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.actor.GhostState;
import de.amr.games.pacman.actor.PacMan;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.view.play.PlayViewX;

public class EscapeIntoCornerTestController implements ViewController {

	private final PacManGame game;
	private final PlayViewX view;
	private final PacMan pacMan;
	private final Ghost blinky;

	public EscapeIntoCornerTestController() {
		Maze maze = new Maze(Assets.text("maze.txt"));
		game = new PacManGame(maze);
		pacMan = game.getActors().getPacMan();
		blinky = game.getActors().getBlinky();
		view = new PlayViewX(game);
		view.setShowRoutes(true);
		view.setShowGrid(false);
		view.setShowStates(true);
		view.setScoresVisible(false);
	}

	@Override
	public void init() {
		game.setLevel(1);
		game.getMaze().tiles().filter(game.getMaze()::isFood).forEach(game::eatFoodAtTile);
		game.getActors().setActive(pacMan, true);
		pacMan.init();
		game.getActors().getGhosts().filter(ghost -> ghost != blinky)
				.forEach(ghost -> game.getActors().setActive(ghost, false));
		blinky.initGhost();
		blinky.setState(GhostState.FRIGHTENED);
	}

	@Override
	public void update() {
		pacMan.update();
		blinky.update();
		view.update();
	}

	@Override
	public View currentView() {
		return view;
	}
}