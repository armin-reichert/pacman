package de.amr.games.pacman.test.navigation;

import static de.amr.games.pacman.actor.behavior.ghost.GhostSteerings.fleeingToSafeCorner;

import de.amr.easy.game.view.View;
import de.amr.easy.game.view.ViewController;
import de.amr.games.pacman.actor.PacManGameCast;
import de.amr.games.pacman.actor.GhostState;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.view.play.PlayView;

public class EscapeIntoCornerTestUI extends PlayView implements ViewController {

	public EscapeIntoCornerTestUI(PacManGame game, PacManGameCast ensemble) {
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
		cast.pacMan.activate();
		cast.pacMan.init();
		cast.blinky.activate();
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