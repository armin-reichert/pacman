package de.amr.games.pacman.actor.steering.ghost;

import static de.amr.games.pacman.model.Direction.DOWN;
import static de.amr.games.pacman.model.Direction.UP;

import de.amr.games.pacman.actor.core.MovingActor;
import de.amr.games.pacman.actor.steering.Steering;

/**
 * Lets an actor (e.g. a ghost) jump up and down relative to a specified position.
 * 
 * @author Armin Reichert
 */
public class JumpingUpAndDown implements Steering {

	private final MovingActor<?> actor;
	private final float baseY;

	public JumpingUpAndDown(MovingActor<?> actor, float baseY) {
		this.actor = actor;
		this.baseY = baseY;
	}

	@Override
	public void steer() {
		float dy = actor.tf.getPosition().y - baseY;
		if (dy < -4) {
			actor.setWishDir(DOWN);
		}
		else if (dy > 3) {
			actor.setWishDir(UP);
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