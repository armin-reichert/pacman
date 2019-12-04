package de.amr.games.pacman.test.navigation;

import java.awt.Color;
import java.awt.event.KeyEvent;

import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.view.View;
import de.amr.easy.game.view.ViewController;
import de.amr.games.pacman.actor.Ensemble;
import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.actor.GhostState;
import de.amr.games.pacman.controller.event.GhostUnlockedEvent;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.theme.ClassicPacManTheme;
import de.amr.games.pacman.view.play.PlayView;

public class ScatteringTestUI extends PlayView implements ViewController {

	public ScatteringTestUI(PacManGame game, Ensemble ensemble) {
		super(game, ensemble, new ClassicPacManTheme());
		showRoutes = true;
		showStates = true;
		showScores = false;
	}

	@Override
	public void init() {
		super.init();
		game.levelNumber = 1;
		game.maze.removeFood();
		ensemble.ghosts().forEach(ghost -> {
			ghost.activate();
			ghost.init();
			ghost.fnNextState = () -> GhostState.SCATTERING;
		});
		showInfoText("Press SPACE to start", Color.YELLOW);
	}

	@Override
	public void update() {
		if (Keyboard.keyPressedOnce(KeyEvent.VK_SPACE)) {
			ensemble.activeGhosts().forEach(ghost -> ghost.process(new GhostUnlockedEvent()));
			hideInfoText();
		}
		ensemble.activeGhosts().forEach(Ghost::update);
		super.update();
	}

	@Override
	public View currentView() {
		return this;
	}
}