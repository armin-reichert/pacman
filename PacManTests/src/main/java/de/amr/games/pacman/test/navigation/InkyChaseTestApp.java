package de.amr.games.pacman.test.navigation;

import static de.amr.games.pacman.controller.actor.GhostState.CHASING;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.util.stream.Stream;

import de.amr.easy.game.Application;
import de.amr.easy.game.config.AppSettings;
import de.amr.easy.game.input.Keyboard;
import de.amr.games.pacman.controller.actor.Ghost;
import de.amr.games.pacman.controller.actor.PacManState;
import de.amr.games.pacman.controller.event.GhostUnlockedEvent;
import de.amr.games.pacman.model.world.Tile;

public class InkyChaseTestApp extends Application {

	public static void main(String[] args) {
		launch(InkyChaseTestApp.class, args);
	}

	@Override
	protected void configure(AppSettings settings) {
		settings.width = 28 * Tile.SIZE;
		settings.height = 36 * Tile.SIZE;
		settings.scale = 2;
		settings.title = "Inky Chasing";
	}

	@Override
	public void init() {
		setController(new InkyChaseTestUI());
	}
}

class InkyChaseTestUI extends TestUI {

	public InkyChaseTestUI() {
		showRoutes = true;
		showStates = false;
		showScores = false;
		showGrid = false;
	}

	@Override
	public void init() {
		super.init();
		world.eatFood();
		theme.snd_ghost_chase().volume(0);
		Stream.of(world.pacMan(), world.inky(), world.blinky()).forEach(actor -> world.takePart(actor, true));
		world.ghostsOnStage().forEach(ghost -> {
			ghost.subsequentState = CHASING;
		});
		showMessage("Press SPACE to start", Color.WHITE);
	}

	@Override
	public void update() {
		if (Keyboard.keyPressedOnce(KeyEvent.VK_SPACE)) {
			world.ghostsOnStage().forEach(ghost -> ghost.process(new GhostUnlockedEvent()));
			world.pacMan().setState(PacManState.EATING);
			clearMessage();
		}
		world.pacMan().update();
		world.ghostsOnStage().forEach(Ghost::update);
		super.update();
	}
}