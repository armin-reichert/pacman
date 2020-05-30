package de.amr.games.pacman.controller.actor.steering.ghost;

import static de.amr.games.pacman.model.Direction.DOWN;
import static de.amr.games.pacman.model.Direction.UP;

import de.amr.games.pacman.controller.actor.Ghost;
import de.amr.games.pacman.controller.actor.steering.Steering;

/**
 * Lets a ghost jump up and down relative to a specified position.
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
			ghost.setWishDir(DOWN);
		} else if (dy > 3) {
			ghost.setWishDir(UP);
		}
	}

	@Override
	public boolean requiresGridAlignment() {
		return false;
	}
}