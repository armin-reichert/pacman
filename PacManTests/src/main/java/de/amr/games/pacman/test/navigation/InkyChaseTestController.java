package de.amr.games.pacman.test.navigation;

import java.awt.Color;

import de.amr.easy.game.view.View;
import de.amr.easy.game.view.ViewController;
import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.actor.GhostState;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.theme.PacManTheme;
import de.amr.games.pacman.view.play.PlayViewXtended;

public class InkyChaseTestController implements ViewController {

	private final PacManGame game;
	private final PlayViewXtended view;

	public InkyChaseTestController(PacManTheme theme) {
		game = new PacManGame(theme);
		game.setLevel(1);
		game.maze.removeFood();
		view = new PlayViewXtended(game, Color.BLACK);
		view.setShowRoutes(true);
		view.setShowGrid(false);
		view.setShowStates(true);
		view.setScoresVisible(false);
	}

	@Override
	public void init() {
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