package de.amr.games.pacman.test.navigation;

import java.awt.Color;
import java.awt.event.KeyEvent;

import de.amr.easy.game.Application;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.view.View;
import de.amr.easy.game.view.VisualController;
import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.actor.GhostState;
import de.amr.games.pacman.actor.PacManGameCast;
import de.amr.games.pacman.controller.event.GhostUnlockedEvent;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.theme.ClassicPacManTheme;
import de.amr.games.pacman.theme.PacManTheme;
import de.amr.games.pacman.view.play.PlayView;

public class InkyChaseTestApp extends Application {

	public static void main(String[] args) {
		launch(new InkyChaseTestApp(), args);
	}

	public InkyChaseTestApp() {
		settings.title = "Inky Chasing";
		settings.width = 28 * Tile.SIZE;
		settings.height = 36 * Tile.SIZE;
		settings.scale = 2;
	}

	@Override
	public void init() {
		PacManGame game = new PacManGame();
		PacManTheme theme = new ClassicPacManTheme();
		PacManGameCast cast = new PacManGameCast(game, theme);
		setController(new InkyChaseTestUI(cast));
	}
}

class InkyChaseTestUI extends PlayView implements VisualController {

	public InkyChaseTestUI(PacManGameCast cast) {
		super(cast);
		showRoutes(true);
		showStates(false);
		setShowScores(false);
		showGrid(false);
	}

	@Override
	public void init() {
		super.init();
		game().init();
		maze().removeFood();
		theme().snd_ghost_chase().volume(0);
		cast().putOnStage(cast().pacMan);
		cast().putOnStage(cast().inky);
		cast().putOnStage(cast().blinky);
		cast().ghostsOnStage().forEach(ghost -> {
			ghost.nextState = GhostState.CHASING;
		});
		messageColor = Color.YELLOW;
		messageText = "Press SPACE to start";
	}

	@Override
	public void update() {
		if (Keyboard.keyPressedOnce(KeyEvent.VK_SPACE)) {
			cast().ghostsOnStage().forEach(ghost -> ghost.process(new GhostUnlockedEvent()));
			messageText = null;
		}
		cast().pacMan.update();
		cast().ghostsOnStage().forEach(Ghost::update);
		super.update();
	}

	@Override
	public View currentView() {
		return this;
	}
}