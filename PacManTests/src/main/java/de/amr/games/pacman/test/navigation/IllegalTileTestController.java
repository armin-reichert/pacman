package de.amr.games.pacman.test.navigation;

import de.amr.easy.game.view.View;
import de.amr.easy.game.view.ViewController;
import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.actor.GhostState;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.view.play.PlayViewX;

public class IllegalTileTestController implements ViewController {

	private final PacManGame game;
	private final PlayViewX view;
	private final Ghost blinky;

	public IllegalTileTestController() {
		game = new PacManGame();
		game.getPacMan().setVisible(false);
		blinky = game.getBlinky();
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
		game.getAllGhosts().filter(ghost -> ghost != blinky).forEach(ghost -> game.setActive(ghost, false));
		blinky.initGhost();
		blinky.setBehavior(GhostState.CHASING, blinky.headFor(this::getTargetTile));
		blinky.setState(GhostState.CHASING);
	}

	private Tile getTargetTile() {
		return new Tile(game.getMaze().numCols(), game.getMaze().getTunnelRow());
	}

	@Override
	public void update() {
		blinky.update();
		view.update();
	}

	@Override
	public View currentView() {
		return view;
	}
}