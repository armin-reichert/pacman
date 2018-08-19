package de.amr.games.pacman.navigation.impl;

import static de.amr.games.pacman.model.Maze.NESW;

import de.amr.games.pacman.actor.core.MazeMover;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.navigation.MazeRoute;
import de.amr.games.pacman.navigation.Navigation;

class Bounce implements Navigation {

	@Override
	public MazeRoute computeRoute(MazeMover bouncer) {
		MazeRoute route = new MazeRoute();
		int currentDir = bouncer.getCurrentDir();
		Tile next = bouncer.computeTileAfterMove(currentDir);
		boolean reflected = bouncer.getMaze().isDoor(next) || bouncer.getMaze().isWall(next);
		route.dir = reflected ? NESW.inv(currentDir) : currentDir;
		return route;
	}
}