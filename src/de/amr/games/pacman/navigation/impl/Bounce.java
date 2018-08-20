package de.amr.games.pacman.navigation.impl;

import static de.amr.games.pacman.model.Maze.NESW;

import de.amr.games.pacman.actor.core.MazeMover;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.navigation.MazeRoute;
import de.amr.games.pacman.navigation.Navigation;

class Bounce implements Navigation {

	@Override
	public MazeRoute computeRoute(MazeMover bouncer) {
		Maze maze = bouncer.getMaze();
		int currentDir = bouncer.getCurrentDir();
		boolean bounce = !bouncer.canMove(currentDir) || maze.isDoor(bouncer.tileAfterMove(currentDir));
		MazeRoute route = new MazeRoute();
		route.dir = bounce ? NESW.inv(currentDir) : currentDir;
		return route;
	}
}