package de.amr.games.pacman.test.navigation;

import static de.amr.games.pacman.actor.GhostState.CHASING;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.util.Optional;

import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.view.View;
import de.amr.easy.game.view.VisualController;
import de.amr.games.pacman.PacManApp;
import de.amr.games.pacman.actor.Cast;
import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.actor.PacManState;
import de.amr.games.pacman.controller.event.GhostUnlockedEvent;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.theme.ArcadeTheme;
import de.amr.games.pacman.theme.Theme;
import de.amr.games.pacman.view.play.PlayView;

public class InkyChaseTestApp extends PacManApp {

	public static void main(String[] args) {
		launch(new InkyChaseTestApp(), args);
	}

	public InkyChaseTestApp() {
		settings.title = "Inky Chasing";
	}

	@Override
	public void init() {
		Game game = new Game();
		Theme theme = new ArcadeTheme();
		Cast cast = new Cast(game, theme);
		setController(new InkyChaseTestUI(cast));
	}
}

class InkyChaseTestUI extends PlayView implements VisualController {

	public InkyChaseTestUI(Cast cast) {
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
		theme().snd_ghost_chase().volume(0);
		cast().setActorOnStage(cast().pacMan);
		cast().setActorOnStage(cast().inky);
		cast().setActorOnStage(cast().blinky);
		cast().ghostsOnStage().forEach(ghost -> {
			ghost.setNextState(CHASING);
		});
		messageColor = Color.YELLOW;
		messageText = "Press SPACE to start";
	}

	@Override
	public void update() {
		if (Keyboard.keyPressedOnce(KeyEvent.VK_SPACE)) {
			cast().ghostsOnStage().forEach(ghost -> ghost.process(new GhostUnlockedEvent()));
			cast().pacMan.setState(PacManState.ALIVE);
			messageText = null;
		}
		cast().pacMan.update();
		cast().ghostsOnStage().forEach(Ghost::update);
		super.update();
	}

	@Override
	public Optional<View> currentView() {
		return Optional.of(this);
	}

}