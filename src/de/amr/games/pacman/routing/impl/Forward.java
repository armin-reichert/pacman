package de.amr.games.pacman.routing.impl;

import de.amr.games.pacman.actor.MazeMover;
import de.amr.games.pacman.routing.MazeRoute;
import de.amr.games.pacman.routing.Navigation;

class Forward implements Navigation {

	@Override
	public MazeRoute computeRoute(MazeMover<?> mover) {
		RouteData result = new RouteData();
		result.dir = mover.getNextDir();
		return result;
	}
}