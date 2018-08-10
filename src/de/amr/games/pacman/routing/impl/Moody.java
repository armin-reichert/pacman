package de.amr.games.pacman.routing.impl;

import static de.amr.easy.util.StreamUtils.randomElement;

import de.amr.games.pacman.actor.MazeMover;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.routing.MazeRoute;
import de.amr.games.pacman.routing.Navigation;

/**
 * Inky's behaviour.
 */
class Moody implements Navigation {

	@Override
	public MazeRoute computeRoute(MazeMover<?> mover) {
		RouteData result = new RouteData();
		result.dir = randomElement(Maze.TOPOLOGY.dirs().filter(dir -> dir != Maze.TOPOLOGY.inv(mover.getDir()))).getAsInt();
		return result;
	}
}