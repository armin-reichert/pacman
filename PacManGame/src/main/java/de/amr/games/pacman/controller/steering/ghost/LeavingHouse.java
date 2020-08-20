package de.amr.games.pacman.controller.steering.ghost;

import static de.amr.games.pacman.model.world.api.Direction.LEFT;
import static de.amr.games.pacman.model.world.api.Direction.RIGHT;
import static de.amr.games.pacman.model.world.api.Direction.UP;

import de.amr.games.pacman.controller.steering.api.Steering;
import de.amr.games.pacman.model.world.components.House;
import de.amr.games.pacman.model.world.components.Tile;
import de.amr.games.pacman.model.world.core.MovingGuy;

public class LeavingHouse implements Steering {

	private final House house;

	public LeavingHouse(House house) {
		this.house = house;
	}

	@Override
	public void steer(MovingGuy entity) {
		Tile exit = Tile.at(house.bed(0).col(), house.bed(0).row());
		int targetX = exit.centerX(), targetY = exit.y();
		if (entity.tf.y <= targetY) {
			entity.tf.y = targetY;
		} else if (Math.round(entity.tf.x) == targetX) {
			entity.tf.x = targetX;
			entity.wishDir = UP;
		} else if (entity.tf.x < targetX) {
			entity.wishDir = RIGHT;
		} else if (entity.tf.x > targetX) {
			entity.wishDir = LEFT;
		}
	}
}