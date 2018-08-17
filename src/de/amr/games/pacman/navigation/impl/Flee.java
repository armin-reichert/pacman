package de.amr.games.pacman.navigation.impl;

import java.util.ArrayList;
import java.util.List;

import de.amr.games.pacman.actor.core.MazeMover;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.navigation.MazeRoute;
import de.amr.games.pacman.navigation.Navigation;

class Flee implements Navigation {

	private final MazeMover chaser;
	private final Maze maze;
	private final List<Tile> corners = new ArrayList<>();

	public Flee(MazeMover chaser) {
		this.chaser = chaser;
		maze = chaser.getMaze();
		corners.add(new Tile(1, 1));
		corners.add(new Tile(maze.numCols() - 2, 1));
		corners.add(new Tile(1, maze.numRows() - 2));
		corners.add(new Tile(maze.numCols() - 2, maze.numRows() - 2));
	}

	@Override
	public MazeRoute computeRoute(MazeMover refugee) {
		MazeRoute route = new MazeRoute();
		if (maze.isTeleportSpace(chaser.getTile()) || maze.isTeleportSpace(refugee.getTile())) {
			route.dir = refugee.getNextDir();
			return route;
		}
		int maxDist = 0;
		Tile farestCorner = null;
		for (Tile corner : corners) {
			int dist = maze.findPath(chaser.getTile(), corner).size();
			if (dist > maxDist) {
				maxDist = dist;
				farestCorner = corner;
			}
		}
		route.path = maze.findPath(refugee.getTile(), farestCorner);
		route.dir = maze.alongPath(route.path).orElse(refugee.getNextDir());
		return route;
	}
}