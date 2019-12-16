package de.amr.games.pacman.actor.behavior.ghost;

import java.util.Collections;

import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.actor.behavior.Steering;
import de.amr.games.pacman.model.Direction;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;

public class EnteringGhostHouse implements Steering<Ghost> {

	final Maze maze;
	final int ghostHomeIndex;

	public EnteringGhostHouse(Maze maze, int ghostHomeIndex) {
		this.maze = maze;
		this.ghostHomeIndex = ghostHomeIndex;
	}

	@Override
	public void steer(Ghost ghost) {
		int bottomY = maze.ghostHome[ghostHomeIndex].row * Tile.SIZE;
		ghost.setTargetTile(maze.ghostHome[ghostHomeIndex]);
		ghost.setTargetPath(Collections.emptyList());
		if (maze.inFrontOfGhostHouseDoor(ghost.tile()) && ghost.nextDir() != Direction.DOWN) {
			ghost.placeAtTile(maze.ghostHome[0], Tile.SIZE / 2, 0);
			ghost.setNextDir(Direction.DOWN);
			return;
		}
		if (ghost.tf.getY() < bottomY) {
			ghost.setNextDir(Direction.DOWN);
			return;
		}
		ghost.tf.setY(bottomY);
		int targetX = maze.ghostHome[ghostHomeIndex].col * Tile.SIZE + Tile.SIZE / 2;
		if (aboutEqual(1, ghost.tf.getX(), targetX)) {
			ghost.setNextDir(null);
		} else if (ghost.tf.getX() < targetX) {
			ghost.setNextDir(Direction.RIGHT);
		} else if (ghost.tf.getX() > targetX) {
			ghost.setNextDir(Direction.LEFT);
		}
	}

	boolean aboutEqual(float tolerance, float f1, float f2) {
		return Math.abs(f1 - f2) <= tolerance;
	}
}