package de.amr.games.pacman.test.navigation;

import de.amr.easy.game.view.View;
import de.amr.easy.game.view.ViewController;
import de.amr.games.pacman.actor.GhostState;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.view.play.PlayViewXtended;

public class LeaveGhostHouseTestUI extends PlayViewXtended implements ViewController {

	public LeaveGhostHouseTestUI(PacManGame game) {
		super(game);
		showRoutes = true;
		showGrid = true;
		showStates = true;
		showScores = false;
	}

	@Override
	public void init() {
		super.init();
		game.level = 1;
		game.maze.removeFood();
		game.pacMan.hide();
		game.ghosts().filter(ghost -> ghost != game.inky).forEach(ghost -> game.setActive(ghost, false));
		game.inky.init();
		game.inky.fnIsUnlocked = () -> true;
		game.inky.fnNextState = () -> GhostState.SCATTERING;
	}

	@Override
	public void update() {
		game.inky.update();
		super.update();
	}

	@Override
	public View currentView() {
		return this;
	}
}