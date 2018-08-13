package de.amr.games.pacman.routing;

import de.amr.games.pacman.actor.TileWorldMover;

public interface Navigation {

	MazeRoute computeRoute(TileWorldMover mover);
}