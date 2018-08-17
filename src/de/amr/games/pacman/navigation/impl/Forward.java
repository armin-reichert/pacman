package de.amr.games.pacman.navigation.impl;

import de.amr.games.pacman.actor.core.MazeMover;
import de.amr.games.pacman.navigation.MazeRoute;
import de.amr.games.pacman.navigation.Navigation;

class Forward implements Navigation {

	@Override
	public MazeRoute computeRoute(MazeMover mover) {
		MazeRoute result = new MazeRoute();
		result.dir = mover.getNextDir();
		return result;
	}
}