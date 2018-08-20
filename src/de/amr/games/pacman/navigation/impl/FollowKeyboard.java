package de.amr.games.pacman.navigation.impl;

import de.amr.easy.game.input.Keyboard;
import de.amr.easy.grid.impl.Top4;
import de.amr.games.pacman.actor.core.MazeMover;
import de.amr.games.pacman.navigation.MazeRoute;
import de.amr.games.pacman.navigation.Navigation;

class FollowKeyboard implements Navigation {

	private final int keyN, keyE, keyS, keyW;

	/**
	 * @param keys
	 *               keyboard codes for North, East, South, West
	 */
	public FollowKeyboard(int... keys) {
		if (keys.length != 4) {
			throw new IllegalArgumentException("Must specify 4 key codes for keyboard steering");
		}
		keyN = keys[0];
		keyE = keys[1];
		keyS = keys[2];
		keyW = keys[3];
	}

	@Override
	public MazeRoute computeRoute(MazeMover mover) {
		MazeRoute result = new MazeRoute();
		if (Keyboard.keyDown(keyN)) {
			result.dir = Top4.N;
		} else if (Keyboard.keyDown(keyE)) {
			result.dir = Top4.E;
		} else if (Keyboard.keyDown(keyS)) {
			result.dir = Top4.S;
		} else if (Keyboard.keyDown(keyW)) {
			result.dir = Top4.W;
		} else {
			result.dir = -1;
		}
		return result;
	}
}