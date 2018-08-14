package de.amr.games.pacman.routing.impl;

import static de.amr.easy.util.StreamUtils.randomElement;

import de.amr.games.pacman.actor.core.MazeMover;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.routing.MazeRoute;
import de.amr.games.pacman.routing.Navigation;

/**
 * Inky's behaviour.
 */
class Moody implements Navigation {

	@Override
	public MazeRoute computeRoute(MazeMover mover) {
		RouteData result = new RouteData();
		result.dir = randomElement(Maze.NESW.dirs().filter(dir -> dir != Maze.NESW.inv(mover.getDir()))).getAsInt();
		return result;
	}
}