package de.amr.games.pacman.test.navigation;

import static de.amr.games.pacman.actor.GhostState.CHASING;

import de.amr.easy.game.view.View;
import de.amr.easy.game.view.ViewController;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.view.play.PlayViewXtended;

public class OutsideTileTestUI extends PlayViewXtended implements ViewController {

	public OutsideTileTestUI(PacManGame game) {
		super(game);
		showRoutes = true;
		showStates = true;
		scoresVisible = false;
	}

	@Override
	public void init() {
		super.init();
		game.level = 1;
		game.setActive(game.pacMan, false);
		game.setActive(game.pinky, false);
		game.setActive(game.inky, false);
		game.setActive(game.clyde, false);
		game.blinky.fnChasingTarget = () -> game.maze.tileAt(100, game.maze.tunnelRightExit.row);
		game.blinky.init();
		game.blinky.setState(CHASING);
	}

	@Override
	public void update() {
		game.blinky.update();
		super.update();
	}

	@Override
	public View currentView() {
		return this;
	}
}