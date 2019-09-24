package de.amr.games.pacman.test.navigation;

import de.amr.easy.game.view.View;
import de.amr.easy.game.view.ViewController;
import de.amr.games.pacman.actor.GhostState;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.view.play.PlayViewXtended;

public class EscapeIntoCornerTestController implements ViewController {

	private final PacManGame game;
	private final PlayViewXtended view;

	public EscapeIntoCornerTestController() {
		game = new PacManGame();
		view = new PlayViewXtended(game);
		view.setShowRoutes(true);
		view.setShowGrid(false);
		view.setShowStates(true);
		view.setScoresVisible(false);
	}

	@Override
	public void init() {
		game.setLevel(1);
		game.maze.tiles().filter(game.maze::isFood).forEach(game::eatFoodAtTile);
		game.setActive(game.pacMan, true);
		game.pacMan.init();
		game.ghosts().filter(ghost -> ghost != game.blinky).forEach(ghost -> game.setActive(ghost, false));
		game.blinky.initGhost();
		game.blinky.setState(GhostState.FRIGHTENED);
	}

	@Override
	public void update() {
		game.pacMan.update();
		game.blinky.update();
		view.update();
	}

	@Override
	public View currentView() {
		return view;
	}
}