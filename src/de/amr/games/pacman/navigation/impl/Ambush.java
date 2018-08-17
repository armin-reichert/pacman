package de.amr.games.pacman.navigation.impl;

import static de.amr.games.pacman.model.Content.WALL;

import java.util.Optional;

import de.amr.games.pacman.actor.core.MazeMover;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.navigation.MazeRoute;
import de.amr.games.pacman.navigation.Navigation;

/**
 * Ambush the victim in the maze.
 */
class Ambush implements Navigation {

	private final MazeMover victim;
	private final Maze maze;

	public Ambush(MazeMover victim) {
		this.victim = victim;
		this.maze = victim.getMaze();
	}

	@Override
	public MazeRoute computeRoute(MazeMover ambusher) {
		MazeRoute route = new MazeRoute();
		if (maze.isTeleportSpace(victim.getTile())) {
			route.dir = ambusher.getNextDir();
			return route;
		}
		Optional<Tile> fourAhead = ahead(4, victim);
		if (fourAhead.isPresent() && maze.getContent(fourAhead.get()) != WALL) {
			route.path = maze.findPath(ambusher.getTile(), fourAhead.get());
		} else {
			route.path = maze.findPath(ambusher.getTile(), victim.getTile());
		}
		route.dir = maze.alongPath(route.path).orElse(ambusher.getNextDir());
		return route;
	}

	private Optional<Tile> ahead(int n, MazeMover refugee) {
		Tile current = refugee.getTile();
		for (int i = 0; i < n; ++i) {
			Optional<Tile> next = maze.neighborTile(current, refugee.getCurrentDir());
			if (next.isPresent()) {
				current = next.get();
			}
		}
		return Optional.of(current);
	}
}