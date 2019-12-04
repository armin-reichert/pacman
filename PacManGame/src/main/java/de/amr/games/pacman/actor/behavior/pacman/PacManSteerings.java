package de.amr.games.pacman.actor.behavior.pacman;

import static de.amr.games.pacman.model.Maze.NESW;

import de.amr.datastruct.StreamUtils;
import de.amr.easy.game.input.Keyboard;
import de.amr.games.pacman.actor.PacMan;
import de.amr.games.pacman.actor.behavior.Steering;

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
	 * @param keys steering key codes in order N, E, S, W
	 * @return steering using the given keys
	 */
	static Steering<PacMan> steeredByKeys(int... keys) {
		return pacMan -> NESW.dirs().filter(dir -> Keyboard.keyDown(keys[dir])).findAny().ifPresent(pacMan::setNextDir);
	}

	static Steering<PacMan> movingRandomly() {
		return pacMan -> {
			if (pacMan.isStuck()) {
				StreamUtils.permute(NESW.dirs()).filter(dir -> dir != NESW.inv(pacMan.moveDir))
						.filter(dir -> pacMan.canMoveBetween(pacMan.tile(), pacMan.maze.tileToDir(pacMan.tile(), dir))).findFirst()
						.ifPresent(pacMan::setNextDir);
			}
		};
	}

	static Steering<PacMan> avoidGhosts() {
		return new AvoidGhosts();
	}
}
