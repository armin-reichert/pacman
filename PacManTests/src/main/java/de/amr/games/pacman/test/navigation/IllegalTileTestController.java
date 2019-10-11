package de.amr.games.pacman.test.navigation;

import de.amr.easy.game.view.View;
import de.amr.easy.game.view.ViewController;
import de.amr.games.pacman.actor.GhostState;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.view.play.PlayViewXtended;

public class IllegalTileTestController implements ViewController {

	private final PacManGame game;
	private final PlayViewXtended view;

	public IllegalTileTestController() {
		game = new PacManGame();
		game.setLevel(1);
		game.maze.removeFood();
		view = new PlayViewXtended(game);
		view.setShowRoutes(true);
		view.setShowGrid(false);
		view.setShowStates(true);
		view.setScoresVisible(false);
	}

	@Override
	public void init() {
		game.pacMan.setVisible(false);
		game.ghosts().filter(ghost -> ghost != game.blinky)
				.forEach(ghost -> game.setActive(ghost, false));
		game.blinky.initGhost();
		game.blinky.setBehavior(GhostState.CHASING, game.blinky.headFor(this::getTargetTile));
		game.blinky.setState(GhostState.CHASING);
	}

	private Tile getTargetTile() {
		return new Tile(game.maze.numCols(), game.maze.getTunnelRow());
	}

	@Override
	public void update() {
		game.blinky.update();
		view.update();
	}

	@Override
	public View currentView() {
		return view;
	}
}