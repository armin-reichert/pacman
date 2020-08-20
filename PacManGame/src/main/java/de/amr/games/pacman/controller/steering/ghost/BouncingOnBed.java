package de.amr.games.pacman.controller.steering.ghost;

import static de.amr.games.pacman.model.world.api.Direction.DOWN;
import static de.amr.games.pacman.model.world.api.Direction.UP;

import de.amr.games.pacman.controller.steering.api.Steering;
import de.amr.games.pacman.model.world.components.Bed;
import de.amr.games.pacman.model.world.core.MovingGuy;

/**
 * Lets a guy bounce on its bed.
 * 
 * @author Armin Reichert
 */
public class BouncingOnBed implements Steering {

	private final float bedCenterY;

	public BouncingOnBed(Bed bed) {
		bedCenterY = bed.center().y;
	}

	@Override
	public void steer(MovingGuy guy) {
		float dy = guy.tf.y + guy.tf.height / 2 - bedCenterY;
		if (dy < -4) {
			guy.wishDir = DOWN;
		} else if (dy > 5) {
			guy.wishDir = UP;
		}
	}
}