package de.amr.games.pacman.actor.behavior.ghost;

import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.actor.behavior.Steering;
import de.amr.games.pacman.model.Direction;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;

public class LeavingGhostHouse implements Steering<Ghost> {

	final Maze maze;

	public LeavingGhostHouse(Maze maze) {
		this.maze = maze;
	}

	@Override
	public void steer(Ghost ghost) {
		float ghostX = ghost.tf.getX(), ghostY = ghost.tf.getY();
		float middleX = maze.ghostHome[2].col * Tile.SIZE + Tile.SIZE / 2;
		float exitY = maze.ghostHome[0].row * Tile.SIZE;
		if (aboutEqual(1, ghostX, middleX)) {
			ghost.setNextDir(Direction.UP);
		}
		else if (ghostX < middleX) {
			ghost.setNextDir(Direction.RIGHT);
		}
		else if (ghostX > middleX) {
			ghost.setNextDir(Direction.LEFT);
		}
		else if (ghostY <= exitY) {
			ghost.setNextDir(null); // got out
		}
	}

	boolean aboutEqual(float tolerance, float f1, float f2) {
		return Math.abs(f1 - f2) <= tolerance;
	}
}
