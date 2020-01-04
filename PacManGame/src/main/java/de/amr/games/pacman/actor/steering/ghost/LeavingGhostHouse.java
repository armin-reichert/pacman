package de.amr.games.pacman.actor.steering.ghost;

import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.actor.steering.core.Steering;
import de.amr.games.pacman.model.Direction;

/**
 * Steering for ghost leaving the house.
 * 
 * @author Armin Reichert
 */
public class LeavingGhostHouse implements Steering {

	private static boolean aboutEqual(float tolerance, float f1, float f2) {
		return Math.abs(f1 - f2) <= tolerance;
	}

	private final Ghost ghost;
	private boolean complete;

	public LeavingGhostHouse(Ghost ghost) {
		this.ghost = ghost;
	}

	@Override
	public void steer() {
		int targetX = ghost.maze().ghostHouseSeats[0].centerX();
		int targetY = ghost.maze().ghostHouseSeats[0].y();
		if (ghost.tf.getY() <= targetY) {
			complete = true;
		}
		else if (aboutEqual(1, ghost.tf.getX(), targetX)) {
			ghost.tf.setX(targetX);
			ghost.setWishDir(Direction.UP);
		}
		else if (ghost.tf.getX() < targetX) {
			ghost.setWishDir(Direction.RIGHT);
		}
		else if (ghost.tf.getX() > targetX) {
			ghost.setWishDir(Direction.LEFT);
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
	public void force() {
	}

	@Override
	public boolean requiresGridAlignment() {
		return false;
	}

	@Override
	public void enableTargetPathComputation(boolean b) {
	}
}
