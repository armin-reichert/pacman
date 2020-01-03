package de.amr.games.pacman.actor.behavior.ghost;

import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.actor.behavior.common.Steering;
import de.amr.games.pacman.model.Direction;

/**
 * Lets a ghost jump up and down at its place in the house.
 * 
 * @author Armin Reichert
 */
public class JumpingUpAndDown implements Steering {

	private final Ghost ghost;
	private final int placeY;

	public JumpingUpAndDown(Ghost ghost, int place) {
		this.ghost = ghost;
		placeY = ghost.maze().ghostHouseSeats[place].y();
	}

	@Override
	public void steer() {
		float dy = ghost.tf.getPosition().y - placeY;
		if (dy < -4) {
			ghost.setWishDir(Direction.DOWN);
		} else if (dy > 3) {
			ghost.setWishDir(Direction.UP);
		}
	}

	@Override
	public boolean requiresGridAlignment() {
		return false;
	}

	@Override
	public void enableTargetPathComputation(boolean b) {
	}

}