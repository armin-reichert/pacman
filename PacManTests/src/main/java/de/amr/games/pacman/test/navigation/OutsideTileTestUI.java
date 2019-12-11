package de.amr.games.pacman.test.navigation;

import static de.amr.games.pacman.actor.GhostState.CHASING;

import de.amr.easy.game.view.View;
import de.amr.easy.game.view.VisualController;
import de.amr.games.pacman.actor.PacManGameCast;
import de.amr.games.pacman.view.play.PlayView;

public class OutsideTileTestUI extends PlayView implements VisualController {

	public OutsideTileTestUI(PacManGameCast cast) {
		super(cast);
		showRoutes = true;
		showStates = true;
		showScores = false;
	}

	@Override
	public void init() {
		super.init();
		game.newGame();
		cast.theme.snd_ghost_chase().volume(0);
		cast.activate(cast.blinky);
		cast.blinky.fnChasingTarget = () -> game.maze.tileAt(100, game.maze.tunnelExitRight.row);
		cast.blinky.init();
		cast.blinky.setState(CHASING);
	}

	@Override
	public void update() {
		cast.blinky.update();
		super.update();
	}

	@Override
	public View currentView() {
		return this;
	}
}