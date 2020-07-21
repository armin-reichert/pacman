package de.amr.games.pacman.controller.steering.ghost;

import static de.amr.games.pacman.model.world.api.Direction.DOWN;
import static de.amr.games.pacman.model.world.api.Direction.UP;

import de.amr.games.pacman.controller.creatures.ghost.Ghost;
import de.amr.games.pacman.controller.steering.api.Steering;
import de.amr.games.pacman.model.world.api.Tile;
import de.amr.games.pacman.model.world.components.Bed;

public class BouncingOnBed implements Steering {

	private final Ghost ghost;
	private final Bed bed;

	public BouncingOnBed(Ghost ghost, Bed bed) {
		this.ghost = ghost;
		this.bed = bed;
	}

	@Override
	public void steer() {
		float dy = ghost.entity.tf.y + Tile.SIZE / 2 - bed.center().y;
		if (dy < -4) {
			ghost.setWishDir(DOWN);
		} else if (dy > 5) {
			ghost.setWishDir(UP);
		}
	}
}