package de.amr.games.pacman.controller.steering.ghost;

import static de.amr.games.pacman.model.world.api.Direction.DOWN;
import static de.amr.games.pacman.model.world.api.Direction.UP;

import de.amr.games.pacman.controller.steering.api.Guy;
import de.amr.games.pacman.controller.steering.api.Steering;
import de.amr.games.pacman.model.world.components.Bed;

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
	public void steer(Guy guy) {
		float dy = guy.tf.y + guy.tf.height / 2 - bedCenterY;
		if (dy < -4) {
			guy.wishDir = DOWN;
		} else if (dy > 5) {
			guy.wishDir = UP;
		}
	}
}