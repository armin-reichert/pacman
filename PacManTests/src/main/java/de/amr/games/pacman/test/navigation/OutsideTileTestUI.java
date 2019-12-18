package de.amr.games.pacman.test.navigation;

import static de.amr.games.pacman.actor.GhostState.CHASING;

import de.amr.easy.game.view.View;
import de.amr.easy.game.view.VisualController;
import de.amr.games.pacman.actor.PacManGameCast;
import de.amr.games.pacman.actor.behavior.Steerings;
import de.amr.games.pacman.view.play.PlayView;

public class OutsideTileTestUI extends PlayView implements VisualController {

	public OutsideTileTestUI(PacManGameCast cast) {
		super(cast);
		setShowRoutes(true);
		setShowStates(true);
		setShowScores(false);
		setShowGrid(false);
	}

	@Override
	public void init() {
		super.init();
		game.init();
		cast.theme.snd_ghost_chase().volume(0);
		cast.putOnStage(cast.blinky);
		cast.blinky.setSteering(CHASING,
				Steerings.headingFor(() -> game.maze.tileAt(100, game.maze.tunnelExitRight.row)));
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