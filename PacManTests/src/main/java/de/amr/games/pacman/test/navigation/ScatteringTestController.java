package de.amr.games.pacman.test.navigation;

import static de.amr.easy.game.Application.app;

import de.amr.easy.game.view.View;
import de.amr.easy.game.view.ViewController;
import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.actor.GhostState;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.view.play.PlayViewX;

public class ScatteringTestController implements ViewController {

	private final PacManGame game;
	private final PlayViewX view;

	public ScatteringTestController() {
		game = new PacManGame();
		view = new PlayViewX(game);
		view.setShowGrid(true);
		view.setShowRoutes(true);
		view.setShowStates(true);
		view.setScoresVisible(false);
	}

	@Override
	public void init() {
		game.init();
		game.getMaze().tiles().filter(game.getMaze()::isFood).forEach(game::eatFoodAtTile);
		game.pacMan.setVisible(false);
		game.activeGhosts().forEach(ghost -> {
			ghost.initGhost();
			ghost.setState(GhostState.SCATTERING);
		});
		app().clock.setFrequency(60);
	}

	@Override
	public void update() {
		game.activeGhosts().forEach(Ghost::update);
		view.update();
	}

	@Override
	public View currentView() {
		return view;
	}
}