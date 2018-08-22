package de.amr.games.pacman.navigation.impl;

import de.amr.games.pacman.actor.core.MazeMover;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.navigation.MazeRoute;
import de.amr.games.pacman.navigation.Navigation;

class FollowPath implements Navigation {

	private final Tile target;

	public FollowPath(Tile target) {
		this.target = target;
	}

	@Override
	public MazeRoute computeRoute(MazeMover mover) {
		MazeRoute route = new MazeRoute();
		Maze maze = mover.getMaze();
		route.path = maze.findPath(mover.getTile(), target);
		route.dir = maze.alongPath(route.path).orElse(mover.getNextDir());
		return route;
	}
}