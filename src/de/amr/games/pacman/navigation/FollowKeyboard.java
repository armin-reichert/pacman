package de.amr.games.pacman.navigation;

import de.amr.easy.game.input.Keyboard;
import de.amr.easy.grid.impl.Top4;
import de.amr.games.pacman.actor.MazeMover;

class FollowKeyboard implements Navigation {

	private final int keyN, keyE, keyS, keyW;

	public FollowKeyboard(int keyN, int keyE, int keyS, int keyW) {
		this.keyN = keyN;
		this.keyE = keyE;
		this.keyS = keyS;
		this.keyW = keyW;
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