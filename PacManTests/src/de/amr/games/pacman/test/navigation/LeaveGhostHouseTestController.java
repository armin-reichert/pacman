package de.amr.games.pacman.test.navigation;

import de.amr.easy.game.view.View;
import de.amr.easy.game.view.ViewController;
import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.actor.GhostState;
import de.amr.games.pacman.controller.event.StartScatteringEvent;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.view.play.PlayViewX;

public class LeaveGhostHouseTestController implements ViewController {

	private final PacManGame game;
	private final PlayViewX view;
	private final Ghost blinky;

	public LeaveGhostHouseTestController() {
		game = new PacManGame();
		blinky = game.getBlinky();
		view = new PlayViewX(game);
		view.setShowRoutes(true);
		view.setShowGrid(true);
		view.setShowStates(true);
		view.setScoresVisible(false);
	}

	@Override
	public void init() {
		game.setLevel(1);
		game.getMaze().tiles().filter(game.getMaze()::isFood).forEach(game::eatFoodAtTile);
		game.getGhosts().filter(ghost -> ghost != blinky).forEach(ghost -> game.setActive(ghost, false));
		blinky.initGhost();
		blinky.setState(GhostState.SCATTERING);
	}

	@Override
	public void update() {
		blinky.update();
		if (blinky.getState() == GhostState.SAFE && blinky.getStateObject().isTerminated()) {
			blinky.processEvent(new StartScatteringEvent());
		}
		view.update();
	}

	@Override
	public View currentView() {
		return view;
	}
}