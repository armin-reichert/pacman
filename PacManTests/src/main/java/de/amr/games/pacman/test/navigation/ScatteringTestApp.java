package de.amr.games.pacman.test.navigation;

import static de.amr.games.pacman.controller.actor.GhostState.SCATTERING;

import java.awt.Color;
import java.awt.event.KeyEvent;

import de.amr.easy.game.Application;
import de.amr.easy.game.config.AppSettings;
import de.amr.easy.game.input.Keyboard;
import de.amr.games.pacman.controller.actor.Ghost;
import de.amr.games.pacman.controller.event.GhostUnlockedEvent;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.world.Tile;
import de.amr.games.pacman.view.play.PlayView;
import de.amr.games.pacman.view.theme.ArcadeTheme;

public class ScatteringTestApp extends Application {

	public static void main(String[] args) {
		launch(ScatteringTestApp.class, args);
	}

	@Override
	protected void configure(AppSettings settings) {
		settings.width = 28 * Tile.SIZE;
		settings.height = 36 * Tile.SIZE;
		settings.scale = 2;
		settings.title = "Scattering";
	}

	@Override
	public void init() {
		setController(new ScatteringTestUI());
	}
}

class ScatteringTestUI extends PlayView {

	public ScatteringTestUI() {
		super(new Game(), new ArcadeTheme());
		showRoutes = true;
		showStates = false;
		showScores = false;
		showGrid = true;
	}

	@Override
	public void init() {
		super.init();
		game.world.eatFood();
		game.ghosts().forEach(ghost -> {
			game.takePart(ghost);
			ghost.subsequentState = SCATTERING;
		});
		showMessage("Press SPACE to start", Color.WHITE);
	}

	@Override
	public void update() {
		if (Keyboard.keyPressedOnce(KeyEvent.VK_SPACE)) {
			game.ghostsOnStage().forEach(ghost -> ghost.process(new GhostUnlockedEvent()));
			clearMessage();
		}
		game.ghostsOnStage().forEach(Ghost::update);
		super.update();
	}
}