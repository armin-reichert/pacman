package de.amr.games.pacman.navigation.impl;

import de.amr.games.pacman.actor.game.GhostName;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;

public class Scatter extends FollowTargetTile {

	public Scatter(Maze maze, GhostName ghostName) {
		super(maze, () -> getScatteringTarget(maze, ghostName));
	}

	private static Tile getScatteringTarget(Maze maze, GhostName ghostName) {
		switch (ghostName) {
		case Blinky:
			return maze.getBlinkyScatteringTarget();
		case Clyde:
			return maze.getClydeScatteringTarget();
		case Inky:
			return maze.getInkyScatteringTarget();
		case Pinky:
			return maze.getPinkyScatteringTarget();
		}
		throw new IllegalArgumentException("Illegal ghost name: " + ghostName);
	}
}
