package de.amr.games.pacman.test.navigation;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.util.Optional;

import de.amr.easy.game.Application;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.view.View;
import de.amr.easy.game.view.VisualController;
import de.amr.games.pacman.actor.GhostState;
import de.amr.games.pacman.actor.PacManGameCast;
import de.amr.games.pacman.controller.event.GhostUnlockedEvent;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.theme.ClassicPacManTheme;
import de.amr.games.pacman.theme.PacManTheme;
import de.amr.games.pacman.view.play.PlayView;

public class LeaveGhostHouseTestApp extends Application {

	public static void main(String[] args) {
		launch(new LeaveGhostHouseTestApp(), args);
	}

	public LeaveGhostHouseTestApp() {
		settings.title = "Leave Ghost House";
		settings.width = 28 * Tile.SIZE;
		settings.height = 36 * Tile.SIZE;
		settings.scale = 2;
	}

	@Override
	public void init() {
		clock.setFrequency(10);
		PacManGame game = new PacManGame();
		PacManTheme theme = new ClassicPacManTheme();
		PacManGameCast cast = new PacManGameCast(game, theme);
		setController(new LeaveGhostHouseTestUI(cast));
	}
}

class LeaveGhostHouseTestUI extends PlayView implements VisualController {

	public LeaveGhostHouseTestUI(PacManGameCast cast) {
		super(cast);
		showRoutes(true);
		showStates(true);
		setShowScores(false);
		showGrid(true);
	}

	@Override
	public void init() {
		super.init();
		game().init();
		maze().removeFood();
		cast().putOnStage(cast().inky);
		cast().inky.nextState = GhostState.SCATTERING;
		messageColor = Color.YELLOW;
		messageText = "Press SPACE to unlock";
	}

	@Override
	public void update() {
		if (Keyboard.keyPressedOnce(KeyEvent.VK_SPACE)) {
			cast().inky.process(new GhostUnlockedEvent());
			messageText = null;
		}
		cast().inky.update();
		super.update();
	}

	@Override
	public Optional<View> currentView() {
		return Optional.of(this);
	}

}