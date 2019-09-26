package de.amr.games.pacman.test.navigation;

import java.awt.Color;

import de.amr.easy.game.view.View;
import de.amr.easy.game.view.ViewController;
import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.actor.GhostState;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.theme.PacManTheme;
import de.amr.games.pacman.view.play.PlayViewXtended;

public class ScatteringTestController implements ViewController {

	private final PacManGame game;
	private final PlayViewXtended view;

	public ScatteringTestController(PacManTheme theme) {
		game = new PacManGame(theme);
		view = new PlayViewXtended(game, Color.BLACK);
		view.setShowGrid(true);
		view.setShowRoutes(true);
		view.setShowStates(true);
		view.setScoresVisible(false);
	}

	@Override
	public void init() {
		game.init();
		game.maze.removeFood();
		game.pacMan.setVisible(false);
		game.activeGhosts().forEach(ghost -> {
			ghost.initGhost();
			ghost.setState(GhostState.SCATTERING);
		});
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