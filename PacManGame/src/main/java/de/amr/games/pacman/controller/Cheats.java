package de.amr.games.pacman.controller;

import static de.amr.easy.game.Application.LOGGER;

import java.awt.event.KeyEvent;

import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.input.Keyboard.Modifier;
import de.amr.easy.game.view.Controller;
import de.amr.games.pacman.controller.event.GhostKilledEvent;
import de.amr.games.pacman.controller.event.LevelCompletedEvent;
import de.amr.games.pacman.model.PacManGame;

public class Cheats implements Controller {

	private final PacManGameController gameController;
	private final PacManGame game;

	public Cheats(PacManGameController controller) {
		this.gameController = controller;
		game = controller.game;
	}

	@Override
	public void init() {
	}

	@Override
	public void update() {
		/* ALT-"K": Kill all ghosts */
		if (Keyboard.keyPressedOnce(Modifier.ALT, KeyEvent.VK_K)) {
			game.activeGhosts().forEach(ghost -> ghost.processEvent(new GhostKilledEvent(ghost)));
			LOGGER.info(() -> "All ghosts killed");
		}
		/* ALT-"E": Eats all (normal) pellets */
		if (Keyboard.keyPressedOnce(Modifier.ALT, KeyEvent.VK_E)) {
			game.maze.tiles().filter(game.maze::containsPellet).forEach(game::eatFoodAtTile);
			LOGGER.info(() -> "All pellets eaten");
		}
		/* ALT-"L": Selects next level */
		if (Keyboard.keyPressedOnce(Modifier.ALT, KeyEvent.VK_PLUS)) {
			if (gameController.getState() == PacManGameState.PLAYING) {
				LOGGER.info(() -> String.format("Switch to next level (%d)", game.level + 1));
				gameController.enqueue(new LevelCompletedEvent());
			}
		}
		/* ALT-"I": Makes Pac-Man immortable */
		if (Keyboard.keyPressedOnce(Modifier.ALT, KeyEvent.VK_I)) {
			game.immortable = !game.immortable;
			LOGGER.info("Pac-Man immortable = " + game.immortable);
		}
	}
}