package de.amr.games.pacman.test.navigation;

import de.amr.easy.game.view.View;
import de.amr.easy.game.view.ViewController;
import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.actor.GhostState;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.view.play.PlayViewXtended;

public class InkyChaseTestUI extends PlayViewXtended implements ViewController {

	public InkyChaseTestUI(PacManGame game) {
		super(game);
		showRoutes = true;
		showStates = true;
		showScores = false;
	}

	@Override
	public void init() {
		super.init();
		game.level = 1;
		game.maze.removeFood();
		game.theme.snd_ghost_chase().volume(0);
		game.pacMan.init();
		game.setActive(game.pacMan, true);
		game.setActive(game.inky, true);
		game.setActive(game.blinky, true);
		game.activeGhosts().forEach(ghost -> {
			ghost.init();
			ghost.fnIsUnlocked = () -> true;
			ghost.fnNextState = () -> GhostState.CHASING;
		});
	}

	@Override
	public void update() {
		game.pacMan.update();
		game.activeGhosts().forEach(Ghost::update);
		super.update();
	}

	@Override
	public View currentView() {
		return this;
	}
}