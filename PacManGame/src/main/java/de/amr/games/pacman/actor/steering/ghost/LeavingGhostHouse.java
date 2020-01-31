package de.amr.games.pacman.actor.steering.ghost;

import static de.amr.games.pacman.model.Direction.LEFT;
import static de.amr.games.pacman.model.Direction.RIGHT;
import static de.amr.games.pacman.model.Direction.UP;

import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.actor.steering.Steering;
import de.amr.games.pacman.model.Tile;

/**
 * Steers a ghost out of the house.
 * 
 * @author Armin Reichert
 */
public class LeavingGhostHouse implements Steering {

	private final Ghost ghost;
	private boolean complete;

	public LeavingGhostHouse(Ghost ghost) {
		this.ghost = ghost;
	}

	@Override
	public void steer() {
		Tile houseExit = ghost.maze().ghostHouseSeats[0];
		int targetX = houseExit.centerX(), targetY = houseExit.y();
		if (ghost.tf.getY() <= targetY) {
			ghost.tf.setY(targetY);
			complete = true;
		} else if (Math.round(ghost.tf.getX()) == targetX) {
			ghost.tf.setX(targetX);
			ghost.setWishDir(UP);
		} else if (ghost.tf.getX() < targetX) {
			ghost.setWishDir(RIGHT);
		} else if (ghost.tf.getX() > targetX) {
			ghost.setWishDir(LEFT);
		}
	}

	@Override
	public void init() {
		complete = false;
	}

	@Override
	public boolean isComplete() {
		return complete;
	}

	@Override
	public boolean requiresGridAlignment() {
		return false;
	}
}