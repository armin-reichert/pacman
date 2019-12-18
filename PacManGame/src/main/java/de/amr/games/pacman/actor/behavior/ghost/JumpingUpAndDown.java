package de.amr.games.pacman.actor.behavior.ghost;

import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.actor.behavior.Steering;
import de.amr.games.pacman.model.Direction;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;

/**
 * Lets a ghost jump up and down.
 * 
 * @author Armin Reichert
 */
public class JumpingUpAndDown implements Steering<Ghost> {

	private final int baseY;

	public JumpingUpAndDown(Maze maze, int ghostHousePlace) {
		this.baseY = maze.ghostHouseSeats[ghostHousePlace].row * Tile.SIZE;
	}

	@Override
	public void steer(Ghost ghost) {
		float dy = ghost.tf.getPosition().y - baseY;
		if (dy < -3) {
			ghost.setNextDir(Direction.DOWN);
		} else if (dy > 3) {
			ghost.setNextDir(Direction.UP);
		} else {
			ghost.setNextDir(ghost.moveDir());
		}
	}
}