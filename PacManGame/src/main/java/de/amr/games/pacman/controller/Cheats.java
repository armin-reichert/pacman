package de.amr.games.pacman.controller;

import static de.amr.easy.game.Application.LOGGER;
import static de.amr.games.pacman.actor.GhostState.CHASING;
import static de.amr.games.pacman.actor.GhostState.FRIGHTENED;
import static de.amr.games.pacman.actor.GhostState.SCATTERING;

import java.awt.event.KeyEvent;

import de.amr.easy.game.Application;
import de.amr.easy.game.controller.Lifecycle;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.input.Keyboard.Modifier;
import de.amr.games.pacman.PacManApp.PacManAppSettings;
import de.amr.games.pacman.controller.event.GhostKilledEvent;
import de.amr.games.pacman.controller.event.LevelCompletedEvent;
import de.amr.games.pacman.model.Tile;

/**
 * Slave controller handling cheat keys.
 * 
 * @author Armin Reichert
 */
public class Cheats implements Lifecycle {

	private final GameController gameController;

	public Cheats(GameController gameController) {
		this.gameController = gameController;
	}

	@Override
	public void init() {
	}

	@Override
	public void update() {

		/* ALT-"K": Kill all available ghosts */
		if (Keyboard.keyPressedOnce(Modifier.ALT, KeyEvent.VK_K)) {
			gameController.cast().ifPresent(cast -> {
				cast.game().level().ghostsKilledByEnergizer = 0;
				cast.ghostsOnStage().filter(ghost -> ghost.is(CHASING, SCATTERING, FRIGHTENED)).forEach(ghost -> {
					cast.game().scoreKilledGhost(ghost.name());
					ghost.process(new GhostKilledEvent(ghost));
				});
				LOGGER.info(() -> "All ghosts killed");
			});
		}

		/* ALT-"E": Eats all (normal) pellets */
		if (Keyboard.keyPressedOnce(Modifier.ALT, KeyEvent.VK_E)) {
			gameController.cast().ifPresent(cast -> {
				cast.game().maze().tiles().filter(Tile::containsPellet).forEach(tile -> {
					cast.game().eatFoodAt(tile);
					gameController.ghostHouse().ifPresent(House::updateDotCounters);
				});
				LOGGER.info(() -> "All pellets eaten");
			});
		}

		/* ALT-"L": Selects next level */
		if (Keyboard.keyPressedOnce(Modifier.ALT, KeyEvent.VK_PLUS)) {
			gameController.cast().ifPresent(cast -> {
				LOGGER.info(() -> String.format("Switch to next level (%d)", cast.game().level().number + 1));
				gameController.enqueue(new LevelCompletedEvent());
			});
		}

		/* ALT-"I": Makes Pac-Man immortable */
		if (Keyboard.keyPressedOnce(Modifier.ALT, KeyEvent.VK_I)) {
			PacManAppSettings settings = (PacManAppSettings) Application.app().settings;
			settings.pacManImmortable = !settings.pacManImmortable;
			LOGGER.info("Pac-Man immortable = " + settings.pacManImmortable);
		}
	}
}