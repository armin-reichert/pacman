package de.amr.games.pacman.test.navigation;

import static de.amr.games.pacman.actor.behavior.ghost.GhostSteerings.fleeingToSafeCorner;

import de.amr.easy.game.view.View;
import de.amr.easy.game.view.ViewController;
import de.amr.games.pacman.actor.GhostState;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.view.play.PlayViewXtended;

public class EscapeIntoCornerTestUI extends PlayViewXtended implements ViewController {

	public EscapeIntoCornerTestUI(PacManGame game) {
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
		game.setActive(game.pacMan, true);
		game.pacMan.init();
		game.setActive(game.blinky, true);
		game.blinky.setSteering(GhostState.FRIGHTENED, fleeingToSafeCorner(game.pacMan));
		game.blinky.init();
		game.blinky.setState(GhostState.FRIGHTENED);
	}

	@Override
	public void update() {
		game.pacMan.update();
		game.blinky.update();
		super.update();
	}

	@Override
	public View currentView() {
		return this;
	}
}