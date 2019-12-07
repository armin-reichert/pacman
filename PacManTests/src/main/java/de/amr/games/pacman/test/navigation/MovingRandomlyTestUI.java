package de.amr.games.pacman.test.navigation;

import static de.amr.games.pacman.model.PacManGame.TS;

import java.awt.event.KeyEvent;

import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.view.View;
import de.amr.easy.game.view.VisualController;
import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.actor.GhostState;
import de.amr.games.pacman.actor.PacManGameCast;
import de.amr.games.pacman.actor.behavior.common.Steerings;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.view.play.PlayView;

public class MovingRandomlyTestUI extends PlayView implements VisualController {

	boolean running;

	public MovingRandomlyTestUI(PacManGame game, PacManGameCast ensemble) {
		super(game, ensemble);
		showRoutes = true;
		showStates = false;
		showScores = false;
	}

	@Override
	public void init() {
		super.init();
		game.start();
		game.maze.removeFood();
		cast.ghosts().forEach(ghost -> {
			ghost.activate();
			ghost.init();
			ghost.placeAtTile(game.maze.pacManHome, TS / 2, 0);
			ghost.setState(GhostState.CHASING);
			ghost.setSteering(GhostState.CHASING, Steerings.movingRandomlyNoReversing());
		});
		message = "Press SPACE";
	}

	@Override
	public void update() {
		if (Keyboard.keyPressedOnce(KeyEvent.VK_SPACE)) {
			running = true;
			message = null;
		}
		if (running) {
			cast.activeGhosts().forEach(Ghost::update);
		}
		super.update();
	}

	@Override
	public View currentView() {
		return this;
	}
}