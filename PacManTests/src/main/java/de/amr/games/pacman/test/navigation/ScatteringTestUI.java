package de.amr.games.pacman.test.navigation;

import java.awt.Color;
import java.awt.event.KeyEvent;

import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.view.View;
import de.amr.easy.game.view.ViewController;
import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.actor.GhostState;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.view.play.PlayViewXtended;

public class ScatteringTestUI extends PlayViewXtended implements ViewController {

	private boolean locked = true;

	public ScatteringTestUI(PacManGame game) {
		super(game);
		showRoutes = true;
		showStates = true;
		showScores = false;
	}

	@Override
	public void init() {
		super.init();
		game.init();
		game.maze.removeFood();
		game.pacMan.hide();
		game.activeGhosts().forEach(ghost -> {
			ghost.init();
			ghost.fnNextState = () -> GhostState.SCATTERING;
			ghost.fnIsUnlocked = g -> !locked;
		});
		showInfoText("Press SPACE key", Color.YELLOW);
	}

	@Override
	public void update() {
		if (Keyboard.keyPressedOnce(KeyEvent.VK_SPACE)) {
			locked = false;
			hideInfoText();
		}
		game.activeGhosts().forEach(Ghost::update);
		super.update();
	}

	@Override
	public View currentView() {
		return this;
	}
}