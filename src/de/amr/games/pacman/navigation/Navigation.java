package de.amr.games.pacman.navigation;

import de.amr.games.pacman.actor.Actor;

public interface Navigation<T extends Actor> {

	MazeRoute computeRoute(T mover);

	default void computeStaticPath(T mover) {
	}
}