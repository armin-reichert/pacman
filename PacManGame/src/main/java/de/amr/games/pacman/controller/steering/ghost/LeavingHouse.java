package de.amr.games.pacman.controller.steering.ghost;

import static de.amr.games.pacman.model.world.api.Direction.LEFT;
import static de.amr.games.pacman.model.world.api.Direction.RIGHT;
import static de.amr.games.pacman.model.world.api.Direction.UP;

import de.amr.games.pacman.controller.creatures.Guy;
import de.amr.games.pacman.controller.steering.api.Steering;
import de.amr.games.pacman.model.world.api.Tile;
import de.amr.games.pacman.model.world.components.House;

public class LeavingHouse implements Steering {

	private final House house;

	public LeavingHouse(House house) {
		this.house = house;
	}

	@Override
	public void steer(Guy<?> guy) {
		Tile exit = Tile.at(house.bed(0).col(), house.bed(0).row());
		int targetX = exit.centerX(), targetY = exit.y();
		if (guy.tf.y <= targetY) {
			guy.tf.y = targetY;
		} else if (Math.round(guy.tf.x) == targetX) {
			guy.tf.x = targetX;
			guy.wishDir = UP;
		} else if (guy.tf.x < targetX) {
			guy.wishDir = RIGHT;
		} else if (guy.tf.x > targetX) {
			guy.wishDir = LEFT;
		}
	}
}