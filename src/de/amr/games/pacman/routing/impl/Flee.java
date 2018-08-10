package de.amr.games.pacman.routing.impl;

import java.util.ArrayList;
import java.util.List;

import de.amr.games.pacman.actor.MazeMover;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.routing.MazeRoute;
import de.amr.games.pacman.routing.Navigation;

class Flee implements Navigation {

	private final MazeMover<?> chaser;

	public Flee(MazeMover<?> chaser) {
		this.chaser = chaser;
	}

	@Override
	public MazeRoute computeRoute(MazeMover<?> refugee) {
		Maze maze = refugee.maze;
		List<Tile> corners = new ArrayList<>();
		corners.add(new Tile(1, 1));
		corners.add(new Tile(maze.numCols() - 2, 1));
		corners.add(new Tile(1, maze.numRows() - 2));
		corners.add(new Tile(maze.numCols() - 2, maze.numRows() - 2));
		RouteData route = new RouteData();
		if (!maze.isValidTile(chaser.getTile()) || !maze.isValidTile(refugee.getTile())) {
			route.dir = refugee.getNextDir();
			return route; // either chaser or refugee is teleporting
		}
		int max = 0;
		Tile farestCorner = null;
		for (Tile corner : corners) {
			int d = maze.findPath(chaser.getTile(), corner).size();
			if (d > max) {
				max = d;
				farestCorner = corner;
			}
		}
		route.path = maze.findPath(refugee.getTile(), farestCorner);
		route.dir = maze.alongPath(route.path).orElse(refugee.getNextDir());
		return route;
	}
}