package de.amr.games.pacman.navigation.impl;

import static de.amr.easy.util.StreamUtils.randomElement;

import de.amr.games.pacman.actor.core.MazeMover;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.navigation.MazeRoute;
import de.amr.games.pacman.navigation.Navigation;

/**
 * Clyde's behaviour.
 */
class StayBehind implements Navigation {

	@Override
	public MazeRoute computeRoute(MazeMover mover) {
		MazeRoute result = new MazeRoute();
		result.dir = randomElement(Maze.NESW.dirs().filter(dir -> dir != Maze.NESW.inv(mover.getCurrentDir()))).getAsInt();
		return result;
	}
}