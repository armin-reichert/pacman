package de.amr.games.pacman.test.navigation;

import de.amr.easy.game.view.View;
import de.amr.easy.game.view.ViewController;
import de.amr.games.pacman.actor.Ensemble;
import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.view.play.PlayView;

public class JumpingTestUI extends PlayView implements ViewController {

	public JumpingTestUI(PacManGame game, Ensemble ensemble) {
		super(game, ensemble);
		showRoutes = false;
		showStates = true;
		showScores = false;
	}

	@Override
	public void init() {
		super.init();
		game.levelNumber = 1;
		game.maze.removeFood();
		ensemble.ghosts().forEach(ghost -> {
			ghost.activate();
			ghost.init();
		});
	}

	@Override
	public void update() {
		ensemble.activeGhosts().forEach(Ghost::update);
		super.update();
	}

	@Override
	public View currentView() {
		return this;
	}
}