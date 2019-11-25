package de.amr.games.pacman.actor.behavior;

import static de.amr.games.pacman.model.Maze.NESW;

import de.amr.datastruct.StreamUtils;
import de.amr.easy.game.input.Keyboard;

/**
 * Steerings for Pac-Man.
 * 
 * <p>
 * Here we can implenment different steering strategies ("AI") for Pac-Man .
 * 
 * @author Armin Reichert
 */
public interface PacManSteerings {

	/**
	 * @param keys
	 *               steering key codes in order N, E, S, W
	 * @return steering using the given keys
	 */
	default Steering steeredByKeys(int... keys) {
		return pacMan -> NESW.dirs().filter(dir -> Keyboard.keyDown(keys[dir])).findAny()
				.ifPresent(pacMan::setNextDir);
	}

	default Steering movingRandomly() {
		return pacMan -> {
			if (pacMan.isStuck()) {
				StreamUtils
						.permute(NESW.dirs()).filter(dir -> dir != NESW.inv(pacMan.moveDir)).filter(dir -> pacMan
								.canEnterTile(pacMan.currentTile(), pacMan.maze.tileToDir(pacMan.currentTile(), dir)))
						.findFirst().ifPresent(pacMan::setNextDir);
			}
		};
	}
}
