package de.amr.games.pacman.routing.impl;

import de.amr.games.pacman.actor.TileWorldMover;
import de.amr.games.pacman.routing.MazeRoute;
import de.amr.games.pacman.routing.Navigation;

class Forward implements Navigation {

	@Override
	public MazeRoute computeRoute(TileWorldMover mover) {
		RouteData result = new RouteData();
		result.dir = mover.getNextDir();
		return result;
	}
}