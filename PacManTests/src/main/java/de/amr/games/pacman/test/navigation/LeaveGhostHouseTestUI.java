package de.amr.games.pacman.test.navigation;

import de.amr.easy.game.Application;
import de.amr.easy.game.view.View;
import de.amr.easy.game.view.ViewController;
import de.amr.games.pacman.actor.GhostState;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.view.play.PlayViewXtended;

public class LeaveGhostHouseTestUI extends PlayViewXtended implements ViewController {

	public LeaveGhostHouseTestUI(PacManGame game) {
		super(game);
		setShowRoutes(true);
		setShowGrid(true);
		setShowStates(true);
		setScoresVisible(false);
	}

	@Override
	public void init() {
		game.level = 1;
		game.maze.removeFood();
		game.pacMan.hide();
		game.ghosts().filter(ghost -> ghost != game.inky).forEach(ghost -> game.setActive(ghost, false));
		game.inky.init();
		game.inky.visualizePath = true;
		game.inky.fnIsUnlocked = g -> true;
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