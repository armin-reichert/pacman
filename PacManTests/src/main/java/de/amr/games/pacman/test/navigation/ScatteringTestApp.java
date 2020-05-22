package de.amr.games.pacman.test.navigation;

import static de.amr.games.pacman.actor.GhostState.SCATTERING;

import java.awt.event.KeyEvent;

import de.amr.easy.game.config.AppSettings;
import de.amr.easy.game.input.Keyboard;
import de.amr.games.pacman.PacManApp;
import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.controller.event.GhostUnlockedEvent;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.theme.ArcadeTheme;
import de.amr.games.pacman.theme.Theme;
import de.amr.games.pacman.view.play.PlayView;

public class ScatteringTestApp extends PacManApp {

	public static void main(String[] args) {
		launch(ScatteringTestApp.class, args);
	}

	@Override
	protected void configure(AppSettings settings) {
		super.configure(settings);
		settings.title = "Scattering";
	}

	@Override
	public void init() {
		setController(new ScatteringTestUI(new Game(), new ArcadeTheme()));
	}
}

class ScatteringTestUI extends PlayView {

	public ScatteringTestUI(Game game, Theme theme) {
		super(game, theme);
		showRoutes = true;
		showStates = false;
		showScores = false;
		showGrid = false;
	}

	@Override
	public void init() {
		super.init();
		game.maze.removeFood();
		game.ghosts().forEach(ghost -> {
			game.stage.add(ghost);
			ghost.followState = SCATTERING;
		});
		message.text = "Press SPACE to start";
	}

	@Override
	public void update() {
		if (Keyboard.keyPressedOnce(KeyEvent.VK_SPACE)) {
			game.ghostsOnStage().forEach(ghost -> ghost.process(new GhostUnlockedEvent()));
			message.text = "";
		}
		game.ghostsOnStage().forEach(Ghost::update);
		super.update();
	}
}