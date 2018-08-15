package de.amr.games.pacman.routing.impl;

import de.amr.games.pacman.actor.core.MazeMover;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.routing.MazeRoute;
import de.amr.games.pacman.routing.Navigation;

/**
 * Chasing a refugee through the maze.
 */
class Chase implements Navigation {

	private final MazeMover victim;

	public Chase(MazeMover victim) {
		this.victim = victim;
	}

	@Override
	public MazeRoute computeRoute(MazeMover chaser) {
		Maze maze = chaser.getMaze();
		MazeRoute route = new MazeRoute();
		if (maze.isTeleportSpace(victim.getTile())) {
			route.dir = chaser.getNextDir();
			return route;
		}
		route.path = maze.findPath(chaser.getTile(), victim.getTile());
		route.dir = maze.alongPath(route.path).orElse(chaser.getNextDir());
		return route;
	}
}