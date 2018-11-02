package de.amr.games.pacman.test.navigation;

import de.amr.easy.game.view.View;
import de.amr.easy.game.view.ViewController;
import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.actor.GhostState;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.view.play.PlayViewX;

public class LeaveGhostHouseTestController implements ViewController {

	private final PacManGame game;
	private final PlayViewX view;
	private final Ghost inky;

	public LeaveGhostHouseTestController() {
		game = new PacManGame();
		inky = game.getInky();
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
		game.getPacMan().setVisible(false);
		game.getGhosts().filter(ghost -> ghost != inky).forEach(ghost -> game.setActorActive(ghost, false));
		inky.initGhost();
		inky.fnNextState = () -> GhostState.SCATTERING;
		inky.setState(GhostState.SCATTERING);
	}

	@Override
	public void update() {
		inky.update();
		view.update();
	}

	@Override
	public View currentView() {
		return view;
	}
}