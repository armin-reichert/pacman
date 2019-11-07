package de.amr.games.pacman.test.navigation;

import de.amr.easy.game.view.View;
import de.amr.easy.game.view.ViewController;
import de.amr.games.pacman.actor.GhostState;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.view.play.PlayViewXtended;
import de.amr.graph.grid.impl.Top4;

public class OutsideTileTestController implements ViewController {

	private final PacManGame g;
	private final PlayViewXtended view;

	public OutsideTileTestController() {
		g = new PacManGame();
		g.setLevel(1);
		g.maze.removeFood();
		view = new PlayViewXtended(g);
		view.setShowRoutes(true);
		view.setShowGrid(false);
		view.setShowStates(true);
		view.setScoresVisible(false);
	}

	@Override
	public void init() {
		g.pacMan.hide();
		g.ghosts().filter(ghost -> ghost != g.blinky).forEach(ghost -> g.setActive(ghost, false));
		g.blinky.initialize();
		g.blinky.setBehavior(GhostState.CHASING, g.blinky.headingFor(this::getTargetTile));
		g.blinky.setState(GhostState.CHASING);
	}

	private Tile getTargetTile() {
		return g.maze.tileToDir(g.maze.getTeleportRight(), Top4.E);
	}

	@Override
	public void update() {
		g.blinky.update();
		view.update();
	}

	@Override
	public View currentView() {
		return view;
	}
}