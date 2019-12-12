package de.amr.games.pacman.test.navigation;

import static de.amr.games.pacman.actor.behavior.common.Steerings.fleeingToSafeCorner;

import de.amr.easy.game.view.View;
import de.amr.easy.game.view.VisualController;
import de.amr.games.pacman.actor.GhostState;
import de.amr.games.pacman.actor.PacManGameCast;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.view.play.PlayView;

public class EscapeIntoCornerTestUI extends PlayView implements VisualController {

	public EscapeIntoCornerTestUI(PacManGame game, PacManGameCast cast) {
		super(cast);
		showRoutes = true;
		showStates = true;
		showScores = false;
	}

	@Override
	public void init() {
		super.init();
		game.nextLevel();
		game.maze.removeFood();
		cast.activate(cast.pacMan);
		cast.pacMan.init();
		cast.activate(cast.blinky);
		cast.blinky.setSteering(GhostState.FRIGHTENED, fleeingToSafeCorner(cast.pacMan));
		cast.blinky.init();
		cast.blinky.setState(GhostState.FRIGHTENED);
	}

	@Override
	public void update() {
		cast.pacMan.update();
		cast.blinky.update();
		super.update();
	}

	@Override
	public View currentView() {
		return this;
	}
}