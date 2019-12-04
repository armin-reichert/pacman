package de.amr.games.pacman.test.navigation;

import static de.amr.games.pacman.actor.behavior.ghost.GhostSteerings.fleeingToSafeCorner;

import de.amr.easy.game.view.View;
import de.amr.easy.game.view.ViewController;
import de.amr.games.pacman.actor.Ensemble;
import de.amr.games.pacman.actor.GhostState;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.view.play.PlayView;

public class EscapeIntoCornerTestUI extends PlayView implements ViewController {

	public EscapeIntoCornerTestUI(PacManGame game, Ensemble ensemble) {
		super(game, ensemble);
		showRoutes = true;
		showStates = true;
		showScores = false;
	}

	@Override
	public void init() {
		super.init();
		game.levelNumber = 1;
		game.maze.removeFood();
		ensemble.pacMan.activate();
		ensemble.pacMan.init();
		ensemble.blinky.activate();
		ensemble.blinky.setSteering(GhostState.FRIGHTENED, fleeingToSafeCorner(ensemble.pacMan));
		ensemble.blinky.init();
		ensemble.blinky.setState(GhostState.FRIGHTENED);
	}

	@Override
	public void update() {
		ensemble.pacMan.update();
		ensemble.blinky.update();
		super.update();
	}

	@Override
	public View currentView() {
		return this;
	}
}