package de.amr.games.pacman.routing;

import de.amr.games.pacman.actor.MazeMover;

public interface Navigation {

	MazeRoute computeRoute(MazeMover<?> mover);
}