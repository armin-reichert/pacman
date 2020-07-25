package de.amr.games.pacman.controller.steering.ghost;

import static de.amr.games.pacman.model.world.api.Direction.LEFT;
import static de.amr.games.pacman.model.world.api.Direction.RIGHT;
import static de.amr.games.pacman.model.world.api.Direction.UP;

import de.amr.games.pacman.controller.creatures.ghost.Ghost;
import de.amr.games.pacman.controller.steering.api.Steering;
import de.amr.games.pacman.model.world.api.Tile;
import de.amr.games.pacman.model.world.components.House;

public class LeavingHouse implements Steering<Ghost> {

	private final House house;

	public LeavingHouse(House house) {
		this.house = house;
	}

	@Override
	public void steer(Ghost ghost) {
		Tile exit = Tile.at(house.bed(0).col(), house.bed(0).row());
		int targetX = exit.centerX(), targetY = exit.y();
		if (ghost.entity.tf.y <= targetY) {
			ghost.entity.tf.y = targetY;
		} else if (Math.round(ghost.entity.tf.x) == targetX) {
			ghost.entity.tf.x = targetX;
			ghost.setWishDir(UP);
		} else if (ghost.entity.tf.x < targetX) {
			ghost.setWishDir(RIGHT);
		} else if (ghost.entity.tf.x > targetX) {
			ghost.setWishDir(LEFT);
		}
	}
}