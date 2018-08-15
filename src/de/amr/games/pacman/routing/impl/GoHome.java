package de.amr.games.pacman.routing.impl;

import de.amr.games.pacman.actor.core.MazeMover;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.routing.MazeRoute;
import de.amr.games.pacman.routing.Navigation;

class GoHome implements Navigation {

	@Override
	public MazeRoute computeRoute(MazeMover mover) {
		MazeRoute route = new MazeRoute();
		Maze maze = mover.getMaze();
		route.path = maze.findPath(mover.getTile(), mover.getHome());
		route.dir = maze.alongPath(route.path).orElse(mover.getNextDir());
		return route;
	}
}