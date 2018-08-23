package de.amr.games.pacman.navigation;

import static de.amr.games.pacman.model.Maze.NESW;

import de.amr.games.pacman.actor.MazeMover;

class Bounce implements Navigation {

	@Override
	public MazeRoute computeRoute(MazeMover bouncer) {
		MazeRoute route = new MazeRoute();
		route.dir = bouncer.isStuck() ? NESW.inv(bouncer.getCurrentDir()) : bouncer.getCurrentDir();
		return route;
	}
}