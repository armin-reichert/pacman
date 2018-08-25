package de.amr.games.pacman.navigation;

import de.amr.games.pacman.actor.MazeMover;

public interface Navigation {

	MazeRoute computeRoute(MazeMover mover);

	default void prepareRoute(MazeMover mover) {
	}
}