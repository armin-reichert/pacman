package de.amr.games.pacman.test.navigation;

import de.amr.easy.game.view.View;
import de.amr.easy.game.view.ViewController;
import de.amr.games.pacman.actor.GhostState;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.view.play.PlayViewXtended;
import de.amr.graph.grid.impl.Top4;

public class OutsideTileTestUI extends PlayViewXtended implements ViewController {

	public OutsideTileTestUI(PacManGame game) {
		super(game);
		setShowRoutes(true);
		setShowGrid(false);
		setShowStates(true);
		setScoresVisible(false);
	}

	@Override
	public void init() {
		game.level = 1;
		game.maze.removeFood();
		game.pacMan.hide();
		game.ghosts().filter(ghost -> ghost != game.blinky).forEach(ghost -> game.setActive(ghost, false));
		game.blinky.init();
		game.blinky.setBehavior(GhostState.CHASING, game.blinky.headingFor(this::getTargetTile));
		game.blinky.setState(GhostState.CHASING);
	}

	private Tile getTargetTile() {
		return game.maze.tileToDir(game.maze.tunnelRightExit, Top4.E);
	}

	@Override
	public void update() {
		game.blinky.update();
		super.update();
	}

	@Override
	public View currentView() {
		return this;
	}
}