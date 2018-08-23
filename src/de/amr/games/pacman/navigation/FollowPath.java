package de.amr.games.pacman.navigation;

import de.amr.games.pacman.actor.MazeMover;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;

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