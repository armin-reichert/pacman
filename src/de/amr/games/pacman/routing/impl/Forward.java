package de.amr.games.pacman.routing.impl;

import de.amr.games.pacman.actor.core.MazeMover;
import de.amr.games.pacman.routing.MazeRoute;
import de.amr.games.pacman.routing.Navigation;

class Forward implements Navigation {

	@Override
	public MazeRoute computeRoute(MazeMover mover) {
		MazeRoute result = new MazeRoute();
		result.dir = mover.getNextDir();
		return result;
	}
}