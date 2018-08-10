package de.amr.games.pacman.routing.impl;

import de.amr.games.pacman.actor.MazeMover;
import de.amr.games.pacman.routing.MazeRoute;
import de.amr.games.pacman.routing.Navigation;

/**
 * Chasing a refugee through the maze.
 */
class Chase implements Navigation {

	private final MazeMover<?> victim;

	public Chase(MazeMover<?> victim) {
		this.victim = victim;
	}

	@Override
	public MazeRoute computeRoute(MazeMover<?> chaser) {
		RouteData route = new RouteData();
		if (victim.isOutsideMaze()) {
			route.dir = chaser.getNextDir();
			return route;
		}
		route.path = chaser.maze.findPath(chaser.getTile(), victim.getTile());
		route.dir = chaser.maze.alongPath(route.path).orElse(chaser.getNextDir());
		return route;
	}
}