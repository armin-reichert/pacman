package de.amr.games.pacman.navigation;

import de.amr.games.pacman.actor.core.MazeMover;

class Forward implements Navigation {

	@Override
	public MazeRoute computeRoute(MazeMover mover) {
		MazeRoute result = new MazeRoute();
		result.dir = mover.getCurrentDir();
		return result;
	}
}