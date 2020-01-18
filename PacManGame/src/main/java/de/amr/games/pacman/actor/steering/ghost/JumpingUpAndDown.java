package de.amr.games.pacman.actor.steering.ghost;

import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.actor.steering.Steering;
import de.amr.games.pacman.model.Direction;

/**
 * Lets a ghost jump up and down at its place in the house.
 * 
 * @author Armin Reichert
 */
public class JumpingUpAndDown implements Steering {

	private final Ghost ghost;
	private final float baseY;

	public JumpingUpAndDown(Ghost ghost, float baseY) {
		this.ghost = ghost;
		this.baseY = baseY;
	}

	@Override
	public void steer() {
		float dy = ghost.tf.getPosition().y - baseY;
		if (dy < -4) {
			ghost.setWishDir(Direction.DOWN);
		} else if (dy > 3) {
			ghost.setWishDir(Direction.UP);
		}
	}

	@Override
	public void init() {
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