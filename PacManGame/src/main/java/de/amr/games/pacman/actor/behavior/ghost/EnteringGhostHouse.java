package de.amr.games.pacman.actor.behavior.ghost;

import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.actor.behavior.Steering;
import de.amr.games.pacman.model.Direction;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;

public class EnteringGhostHouse implements Steering<Ghost> {

	final Maze maze;
	final Tile targetTile;

	public EnteringGhostHouse(Maze maze, Tile targetTile) {
		this.maze = maze;
		this.targetTile = targetTile;
	}

	@Override
	public void steer(Ghost ghost) {
		int bottom = maze.ghostHome[2].row * Tile.SIZE;
		ghost.setTargetTile(null);
		if (maze.inFrontOfGhostHouseDoor(ghost.tile()) && ghost.nextDir() != Direction.DOWN) {
			ghost.placeAtTile(maze.ghostHome[0], Tile.SIZE / 2, 0);
			ghost.setNextDir(Direction.DOWN);
		}
		else if (ghost.tf.getY() >= bottom) {
			int targetX = targetTile.col * Tile.SIZE + Tile.SIZE / 2;
			if (aboutEqual(1, ghost.tf.getX(), targetX)) {
				ghost.setNextDir(null);
			}
			else if (ghost.tf.getX() < targetX) {
				ghost.setNextDir(Direction.RIGHT);
			}
			else if (ghost.tf.getX() > targetX) {
				ghost.setNextDir(Direction.LEFT);
			}
		}
	}

	boolean aboutEqual(float tolerance, float f1, float f2) {
		return Math.abs(f1 - f2) <= tolerance;
	}
}