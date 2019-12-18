package de.amr.games.pacman.actor.behavior.ghost;

import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.actor.behavior.Steering;
import de.amr.games.pacman.model.Direction;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;

/**
 * Steering for ghost leaving the house.
 * 
 * @author Armin Reichert
 */
public class LeavingGhostHouse implements Steering<Ghost> {

	private static boolean aboutEqual(float tolerance, float f1, float f2) {
		return Math.abs(f1 - f2) <= tolerance;
	}

	private final Maze maze;

	public LeavingGhostHouse(Maze maze) {
		this.maze = maze;
	}

	@Override
	public void steer(Ghost ghost) {
		int targetX = maze.ghostHouseSeats[0].col * Tile.SIZE + Tile.SIZE / 2;
		int targetY = maze.ghostHouseSeats[0].row * Tile.SIZE;
		if (aboutEqual(1, ghost.tf.getX(), targetX)) {
			ghost.tf.setX(targetX);
			ghost.setNextDir(Direction.UP);
		}
		else if (ghost.tf.getX() < targetX) {
			ghost.setNextDir(Direction.RIGHT);
		}
		else if (ghost.tf.getX() > targetX) {
			ghost.setNextDir(Direction.LEFT);
		}
		else if (ghost.tf.getY() <= targetY) {
			ghost.setNextDir(null); // got out
		}
	}
}
