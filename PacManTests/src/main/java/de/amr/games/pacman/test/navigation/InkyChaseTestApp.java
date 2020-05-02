package de.amr.games.pacman.test.navigation;

import static de.amr.games.pacman.actor.GhostState.CHASING;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.util.Optional;
import java.util.stream.Stream;

import de.amr.easy.game.config.AppSettings;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.view.View;
import de.amr.easy.game.view.VisualController;
import de.amr.games.pacman.PacManApp;
import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.actor.PacManState;
import de.amr.games.pacman.controller.event.GhostUnlockedEvent;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.theme.ArcadeTheme;
import de.amr.games.pacman.theme.Theme;
import de.amr.games.pacman.view.play.PlayView;

public class InkyChaseTestApp extends PacManApp {

	public static void main(String[] args) {
		launch(InkyChaseTestApp.class, args);
	}

	@Override
	protected void configure(AppSettings settings) {
		super.configure(settings);
		settings.title = "Inky Chasing";
	}

	@Override
	public void init() {
		Game game = new Game();
		Theme theme = new ArcadeTheme();
		setController(new InkyChaseTestUI(game, theme));
	}
}

class InkyChaseTestUI extends PlayView implements VisualController {

	public InkyChaseTestUI(Game game, Theme theme) {
		super(game, theme);
		showRoutes = () -> true;
		showStates = () -> false;
		showScores = () -> false;
		showGrid = () -> false;
	}

	@Override
	public void init() {
		super.init();
		game.maze.removeFood();
		theme.snd_ghost_chase().volume(0);
		Stream.of(game.pacMan, game.inky, game.blinky).forEach(game.stage::add);
		game.ghostsOnStage().forEach(ghost -> {
			ghost.followState = CHASING;
		});
		messageColor = Color.YELLOW;
		messageText = "Press SPACE to start";
	}

	@Override
	public void update() {
		if (Keyboard.keyPressedOnce(KeyEvent.VK_SPACE)) {
			game.ghostsOnStage().forEach(ghost -> ghost.process(new GhostUnlockedEvent()));
			game.pacMan.setState(PacManState.EATING);
			messageText = null;
		}
		game.pacMan.update();
		game.ghostsOnStage().forEach(Ghost::update);
		super.update();
	}

	@Override
	public Optional<View> currentView() {
		return Optional.of(this);
	}

}