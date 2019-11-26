package de.amr.games.pacman.actor.behavior;

import static de.amr.games.pacman.model.Maze.NESW;

import de.amr.datastruct.StreamUtils;
import de.amr.easy.game.input.Keyboard;
import de.amr.games.pacman.actor.PacMan;
import de.amr.games.pacman.model.Maze;

/**
 * Steerings for Pac-Man.
 * 
 * <p>
 * Here we can implement different steering strategies ("AI") for Pac-Man .
 * 
 * @author Armin Reichert
 */
public interface PacManSteerings {

	/**
	 * @param keys
	 *               steering key codes in order N, E, S, W
	 * @return steering using the given keys
	 */
	default Steering<PacMan> steeredByKeys(int... keys) {
		return pacMan -> NESW.dirs().filter(dir -> Keyboard.keyDown(keys[dir])).findAny()
				.ifPresent(pacMan::setNextDir);
	}

	default Steering<PacMan> movingRandomly() {
		return pacMan -> {
			if (pacMan.isStuck()) {
				StreamUtils
						.permute(NESW.dirs()).filter(dir -> dir != NESW.inv(pacMan.moveDir)).filter(dir -> pacMan
								.canCrossBorder(pacMan.currentTile(), pacMan.maze.tileToDir(pacMan.currentTile(), dir)))
						.findFirst().ifPresent(pacMan::setNextDir);
			}
		};
	}

	default Steering<PacMan> avoidGhosts(Maze maze) {
		return new AvoidGhosts(maze);
	}
}
