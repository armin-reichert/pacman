package de.amr.games.pacman.navigation.impl;

import static de.amr.games.pacman.model.Content.DOOR;
import static de.amr.games.pacman.model.Content.WALL;
import static de.amr.games.pacman.model.Maze.NESW;

import de.amr.games.pacman.actor.core.MazeMover;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.navigation.MazeRoute;
import de.amr.games.pacman.navigation.Navigation;

class Bounce implements Navigation {

	@Override
	public MazeRoute computeRoute(MazeMover bouncer) {
		MazeRoute route = new MazeRoute();
		route.dir = isReflected(bouncer) ? NESW.inv(bouncer.getDir()) : bouncer.getDir();
		return route;
	}

	private boolean isReflected(MazeMover bouncer) {
		Tile nextTile = bouncer.computeTileAfterMove(bouncer.getDir());
		char content = bouncer.getMaze().getContent(nextTile);
		return content == WALL || content == DOOR;
	}
}