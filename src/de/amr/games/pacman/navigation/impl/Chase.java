package de.amr.games.pacman.navigation.impl;

import de.amr.games.pacman.actor.core.MazeMover;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.navigation.MazeRoute;
import de.amr.games.pacman.navigation.Navigation;

/**
 * Chasing a victim through the maze.
 */
class Chase implements Navigation {

	private final MazeMover victim;

	public Chase(MazeMover victim) {
		this.victim = victim;
	}

	@Override
	public MazeRoute computeRoute(MazeMover chaser) {
		MazeRoute route = new MazeRoute();
		Maze maze = chaser.getMaze();
		if (victim.inTeleportSpace()) {
			// chaser cannot see victim
			route.dir = chaser.getNextDir();
		} else {
			route.path = maze.findPath(chaser.getTile(), victim.getTile());
			route.dir = maze.alongPath(route.path).orElse(chaser.getNextDir());
		}
		return route;
	}
}