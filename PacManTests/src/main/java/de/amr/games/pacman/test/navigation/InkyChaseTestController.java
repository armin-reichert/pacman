package de.amr.games.pacman.test.navigation;

import de.amr.easy.game.view.View;
import de.amr.easy.game.view.ViewController;
import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.actor.GhostState;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.view.play.PlayViewX;

public class InkyChaseTestController implements ViewController {

	private final PacManGame game;
	private final PlayViewX view;

	public InkyChaseTestController() {
		game = new PacManGame();
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
		game.setActive(game.pacMan, true);
		game.pacMan.init();
		game.setActive(game.pinky, false);
		game.setActive(game.clyde, false);
		game.activeGhosts().forEach(ghost -> {
			ghost.init();
			ghost.setState(GhostState.CHASING);
		});
	}

	@Override
	public void update() {
		game.pacMan.update();
		game.activeGhosts().forEach(Ghost::update);
		view.update();
	}

	@Override
	public View currentView() {
		return view;
	}
}