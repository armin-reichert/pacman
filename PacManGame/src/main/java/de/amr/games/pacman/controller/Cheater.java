package de.amr.games.pacman.controller;

import static de.amr.easy.game.Application.LOGGER;
import static de.amr.easy.game.Application.app;
import static de.amr.games.pacman.actor.GhostState.CHASING;
import static de.amr.games.pacman.actor.GhostState.FRIGHTENED;
import static de.amr.games.pacman.actor.GhostState.SCATTERING;
import static de.amr.games.pacman.controller.PacManGameState.PLAYING;

import java.awt.event.KeyEvent;

import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.input.Keyboard.Modifier;
import de.amr.games.pacman.actor.PacManGameCast;
import de.amr.games.pacman.controller.event.GhostKilledEvent;
import de.amr.games.pacman.controller.event.LevelCompletedEvent;
import de.amr.games.pacman.model.Tile;

/**
 * Slave controller handling cheat keys.
 * 
 * @author Armin Reichert
 */
public class Cheater {

	private final PacManGameController master;
	private final PacManGameCast cast;

	public Cheater(PacManGameCast cast, PacManGameController master) {
		this.master = master;
		this.cast = cast;
	}

	public void handleCheatKeys() {
		/* ALT-"K": Kill all available ghosts */
		if (Keyboard.keyPressedOnce(Modifier.ALT, KeyEvent.VK_K)) {
			cast.game().level().ghostsKilledByEnergizer = 0;
			cast.ghostsOnStage().filter(ghost -> ghost.is(CHASING, SCATTERING, FRIGHTENED)).forEach(ghost -> {
				cast.game().scoreKilledGhost(ghost.name());
				ghost.process(new GhostKilledEvent(ghost));
			});
			LOGGER.info(() -> "All ghosts killed");
		}
		/* ALT-"E": Eats all (normal) pellets */
		if (Keyboard.keyPressedOnce(Modifier.ALT, KeyEvent.VK_E)) {
			cast.game().maze().tiles().filter(Tile::containsPellet).forEach(tile -> {
				cast.game().eatFoodAt(tile);
				master.ghostHouseDoorMan.updateDotCounters();
			});
			LOGGER.info(() -> "All pellets eaten");
		}
		/* ALT-"L": Selects next level */
		if (Keyboard.keyPressedOnce(Modifier.ALT, KeyEvent.VK_PLUS)) {
			if (master.is(PLAYING)) {
				LOGGER.info(() -> String.format("Switch to next level (%d)", cast.game().level().number + 1));
				master.enqueue(new LevelCompletedEvent());
			}
		}
		/* ALT-"I": Makes Pac-Man immortable */
		if (Keyboard.keyPressedOnce(Modifier.ALT, KeyEvent.VK_I)) {
			boolean immortable = app().settings.getAsBoolean("PacMan.immortable");
			app().settings.set("PacMan.immortable", !immortable);
			LOGGER.info("Pac-Man immortable = " + app().settings.getAsBoolean("PacMan.immortable"));
		}
	}
}
