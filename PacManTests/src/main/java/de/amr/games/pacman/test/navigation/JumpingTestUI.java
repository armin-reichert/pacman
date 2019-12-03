package de.amr.games.pacman.test.navigation;

import de.amr.easy.game.view.View;
import de.amr.easy.game.view.ViewController;
import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.theme.ClassicPacManTheme;
import de.amr.games.pacman.view.play.PlayViewXtended;

public class JumpingTestUI extends PlayViewXtended implements ViewController {

	public JumpingTestUI(PacManGame game) {
		super(game, new ClassicPacManTheme());
		showRoutes = false;
		showStates = true;
		showScores = false;
	}

	@Override
	public void init() {
		super.init();
		game.levelNumber = 1;
		game.maze.removeFood();
		game.ghosts().forEach(ghost -> {
			ghost.activate();
			ghost.init();
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