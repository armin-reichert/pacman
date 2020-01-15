package de.amr.games.pacman.test.navigation;

import static de.amr.games.pacman.actor.GhostState.SCATTERING;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.util.Optional;

import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.view.View;
import de.amr.easy.game.view.VisualController;
import de.amr.games.pacman.PacManApp;
import de.amr.games.pacman.actor.Cast;
import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.controller.event.GhostUnlockedEvent;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.theme.ArcadeTheme;
import de.amr.games.pacman.theme.Theme;
import de.amr.games.pacman.view.play.PlayView;

public class ScatteringTestApp extends PacManApp {

	public static void main(String[] args) {
		launch(new ScatteringTestApp(), args);
	}

	public ScatteringTestApp() {
		settings().title = "Scattering";
	}

	@Override
	public void init() {
		Game game = new Game();
		Theme theme = new ArcadeTheme();
		Cast cast = new Cast(game, theme);
		setController(new ScatteringTestUI(cast));
	}
}

class ScatteringTestUI extends PlayView implements VisualController {

	public ScatteringTestUI(Cast cast) {
		super(cast);
		showRoutes = () -> true;
		showStates = () -> false;
		showScores = () -> false;
		showGrid = () -> false;
	}

	@Override
	public void init() {
		super.init();
		maze().removeFood();
		cast().ghosts().forEach(ghost -> {
			cast().putActorOnStage(ghost);
			ghost.setFollowState(SCATTERING);
		});
		messageColor(Color.YELLOW);
		message("Press SPACE to start");
	}

	@Override
	public void update() {
		if (Keyboard.keyPressedOnce(KeyEvent.VK_SPACE)) {
			cast().ghostsOnStage().forEach(ghost -> ghost.process(new GhostUnlockedEvent()));
			clearMessage();
		}
		cast().ghostsOnStage().forEach(Ghost::update);
		super.update();
	}

	@Override
	public Optional<View> currentView() {
		return Optional.of(this);
	}

}