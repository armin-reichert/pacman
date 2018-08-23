package de.amr.games.pacman.navigation;

import java.util.ArrayList;
import java.util.List;

import de.amr.games.pacman.actor.core.MazeMover;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;

class Flee implements Navigation {

	private final MazeMover chaser;
	private final Maze maze;
	private final List<Tile> corners = new ArrayList<>();

	public Flee(MazeMover chaser) {
		this.chaser = chaser;
		maze = chaser.getMaze();
		corners.add(maze.getTopLeftCorner());
		corners.add(maze.getTopRightCorner());
		corners.add(maze.getBottomLeftCorner());
		corners.add(maze.getBottomRightCorner());
	}

	@Override
	public MazeRoute computeRoute(MazeMover refugee) {
		MazeRoute route = new MazeRoute();
		if (maze.inTeleportSpace(chaser.getTile()) || maze.inTeleportSpace(refugee.getTile())) {
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