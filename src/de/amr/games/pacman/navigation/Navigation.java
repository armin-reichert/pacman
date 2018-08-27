package de.amr.games.pacman.navigation;

import de.amr.games.pacman.actor.MazeMover;

public interface Navigation<T extends MazeMover> {

	MazeRoute computeRoute(T mover);

	default void computeStaticRoute(T mover) {
	}
}