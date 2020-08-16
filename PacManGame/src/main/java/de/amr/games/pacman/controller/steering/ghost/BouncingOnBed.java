package de.amr.games.pacman.controller.steering.ghost;

import static de.amr.games.pacman.model.world.api.Direction.DOWN;
import static de.amr.games.pacman.model.world.api.Direction.UP;

import de.amr.games.pacman.controller.steering.api.Steering;
import de.amr.games.pacman.model.world.api.Tile;
import de.amr.games.pacman.model.world.components.Bed;
import de.amr.games.pacman.model.world.core.MovingGuy;

public class BouncingOnBed implements Steering {

	private final Bed bed;

	public BouncingOnBed(Bed bed) {
		this.bed = bed;
	}

	@Override
	public void steer(MovingGuy entity) {
		float dy = entity.tf.y + Tile.SIZE / 2 - bed.center().y;
		if (dy < -4) {
			entity.wishDir = DOWN;
		} else if (dy > 5) {
			entity.wishDir = UP;
		}
	}
}