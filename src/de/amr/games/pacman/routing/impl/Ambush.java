package de.amr.games.pacman.routing.impl;

import static de.amr.games.pacman.model.Content.WALL;

import java.util.Optional;

import de.amr.games.pacman.actor.MazeMover;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.routing.MazeRoute;
import de.amr.games.pacman.routing.Navigation;

/**
 * Ambush the victim in the maze.
 */
class Ambush implements Navigation {

	private final MazeMover<?> victim;

	public Ambush(MazeMover<?> victim) {
		this.victim = victim;
	}

	@Override
	public MazeRoute computeRoute(MazeMover<?> ambusher) {
		RouteData route = new RouteData();
		if (victim.isOutsideMaze()) {
			route.dir = ambusher.getNextDir();
			return route;
		}
		Maze maze = victim.maze;
		Optional<Tile> fourAhead = ahead(4, victim);
		if (fourAhead.isPresent() && maze.getContent(fourAhead.get()) != WALL) {
			route.path = maze.findPath(ambusher.getTile(), fourAhead.get());
		} else {
			route.path = maze.findPath(ambusher.getTile(), victim.getTile());
		}
		route.dir = maze.alongPath(route.path).orElse(ambusher.getNextDir());
		return route;
	}

	private Optional<Tile> ahead(int n, MazeMover<?> refugee) {
		Tile current = refugee.getTile();
		for (int i = 0; i < n; ++i) {
			Optional<Tile> next = refugee.maze.neighborTile(current, refugee.getDir());
			if (next.isPresent()) {
				current = next.get();
			}
		}
		return Optional.of(current);
	}
}