package de.amr.games.pacman.navigation.impl;

import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;

public class Scatter extends FollowTargetTile {

	public Scatter(Maze maze, Tile scatteringTarget) {
		super(maze, () -> scatteringTarget);
	}
}
