package de.amr.games.pacman.test.navigation;

import de.amr.easy.game.view.View;
import de.amr.easy.game.view.ViewController;
import de.amr.games.pacman.actor.GhostState;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.view.play.PlayViewX;

public class IllegalTileTestController implements ViewController {

	private final PacManGame game;
	private final PlayViewX view;

	public IllegalTileTestController() {
		game = new PacManGame();
		game.pacMan.setVisible(false);
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
		game.ghosts().filter(ghost -> ghost != game.blinky).forEach(ghost -> game.setActive(ghost, false));
		game.blinky.initGhost();
		game.blinky.setBehavior(GhostState.CHASING, game.blinky.headFor(this::getTargetTile));
		game.blinky.setState(GhostState.CHASING);
	}

	private Tile getTargetTile() {
		return new Tile(game.getMaze().numCols(), game.getMaze().getTunnelRow());
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