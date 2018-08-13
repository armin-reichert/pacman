package de.amr.games.pacman.routing.impl;

import de.amr.games.pacman.actor.TileWorldMover;
import de.amr.games.pacman.routing.MazeRoute;
import de.amr.games.pacman.routing.Navigation;

class GoHome implements Navigation {

	@Override
	public MazeRoute computeRoute(TileWorldMover mover) {
		RouteData route = new RouteData();
		route.path = mover.maze.findPath(mover.getTile(), mover.homeTile);
		route.dir = mover.maze.alongPath(route.path).orElse(mover.getNextDir());
		return route;
	}
}