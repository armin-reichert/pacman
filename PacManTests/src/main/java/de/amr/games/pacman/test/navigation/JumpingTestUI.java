package de.amr.games.pacman.test.navigation;

import de.amr.easy.game.view.View;
import de.amr.easy.game.view.ViewController;
import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.view.play.PlayViewXtended;

public class JumpingTestUI extends PlayViewXtended implements ViewController {

	public JumpingTestUI(PacManGame game) {
		super(game);
		showRoutes = false;
		showStates = true;
		showScores = false;
	}

	@Override
	public void init() {
		super.init();
		game.init();
		game.maze.removeFood();
		game.setActive(game.pacMan, false);
		game.activeGhosts().forEach(ghost -> {
			ghost.init();
			ghost.fnIsUnlocked = g -> false;
		});
	}

	@Override
	public void update() {
		game.activeGhosts().forEach(Ghost::update);
		super.update();
	}

	@Override
	public View currentView() {
		return this;
	}
}