package de.amr.games.pacman.routing.impl;

import static de.amr.games.pacman.model.Content.DOOR;
import static de.amr.games.pacman.model.Content.WALL;
import static de.amr.games.pacman.model.Maze.NESW;

import de.amr.games.pacman.actor.TileWorldMover;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.routing.MazeRoute;
import de.amr.games.pacman.routing.Navigation;

class Bounce implements Navigation {

	@Override
	public MazeRoute computeRoute(TileWorldMover bouncer) {
		RouteData route = new RouteData();
		route.dir = isReflected(bouncer) ? NESW.inv(bouncer.getDir()) : bouncer.getDir();
		return route;
	}

	private boolean isReflected(TileWorldMover bouncer) {
		Tile nextTile = bouncer.computeNextTile(bouncer.getTile(), bouncer.getDir());
		if (nextTile.equals(bouncer.getTile())) {
			return false;
		}
		char c = bouncer.maze.getContent(nextTile);
		return c == WALL || c == DOOR;
	}
}