package de.amr.games.pacman.test.navigation;

import static de.amr.games.pacman.actor.GhostState.CHASING;

import de.amr.easy.game.view.View;
import de.amr.easy.game.view.ViewController;
import de.amr.games.pacman.actor.Ensemble;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.view.play.PlayView;

public class OutsideTileTestUI extends PlayView implements ViewController {

	public OutsideTileTestUI(PacManGame game, Ensemble ensemble) {
		super(game, ensemble);
		showRoutes = true;
		showStates = true;
		showScores = false;
	}

	@Override
	public void init() {
		super.init();
		game.levelNumber = 1;
		ensemble.theme.snd_ghost_chase().volume(0);
		ensemble.blinky.activate();
		ensemble.blinky.fnChasingTarget = () -> game.maze.tileAt(100, game.maze.tunnelRightExit.row);
		ensemble.blinky.init();
		ensemble.blinky.setState(CHASING);
	}

	@Override
	public void update() {
		ensemble.blinky.update();
		super.update();
	}

	@Override
	public View currentView() {
		return this;
	}
}