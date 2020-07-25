package de.amr.games.pacman.controller.steering.ghost;

import static de.amr.games.pacman.model.world.api.Direction.DOWN;
import static de.amr.games.pacman.model.world.api.Direction.UP;

import de.amr.games.pacman.controller.creatures.ghost.Ghost;
import de.amr.games.pacman.controller.steering.api.Steering;
import de.amr.games.pacman.model.world.api.Tile;
import de.amr.games.pacman.model.world.components.Bed;

public class BouncingOnBed implements Steering<Ghost> {

	private final Bed bed;

	public BouncingOnBed(Bed bed) {
		this.bed = bed;
	}

	@Override
	public void steer(Ghost ghost) {
		float dy = ghost.tf().y + Tile.SIZE / 2 - bed.center().y;
		if (dy < -4) {
			ghost.setWishDir(DOWN);
		} else if (dy > 5) {
			ghost.setWishDir(UP);
		}
	}
}