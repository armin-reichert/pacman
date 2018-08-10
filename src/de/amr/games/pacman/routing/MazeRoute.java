package de.amr.games.pacman.routing;

import java.util.List;

import de.amr.games.pacman.model.Tile;

public interface MazeRoute {

	int getDirection();

	List<Tile> getPath();
}