package de.amr.games.pacman.test.navigation;

import de.amr.easy.game.view.View;
import de.amr.easy.game.view.ViewController;
import de.amr.games.pacman.actor.GhostState;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.view.play.PlayViewXtended;

public class EscapeIntoCornerTestUI extends PlayViewXtended implements ViewController {

	public EscapeIntoCornerTestUI(PacManGame game) {
		super(game);
		setShowRoutes(true);
		setShowGrid(false);
		setShowStates(true);
		setScoresVisible(false);
	}

	@Override
	public void init() {
		game.level = 1;
		game.maze.removeFood();
		game.pacMan.init();
		game.ghosts().filter(ghost -> ghost != game.blinky).forEach(ghost -> game.setActive(ghost, false));
		game.blinky.setSteering(GhostState.FRIGHTENED, game.blinky.fleeingToSafeCorner(game.pacMan));
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