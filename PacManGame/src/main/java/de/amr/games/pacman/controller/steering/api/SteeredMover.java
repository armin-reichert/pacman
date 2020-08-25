package de.amr.games.pacman.controller.steering.api;

import de.amr.games.pacman.controller.steering.common.MovementController;
import de.amr.games.pacman.model.world.api.Direction;
import de.amr.games.pacman.model.world.api.World;
import de.amr.games.pacman.model.world.components.Tile;
import de.amr.games.pacman.model.world.core.MovingEntity;

/**
 * A moving entity with steering and movement controller.
 * 
 * @author Armin Reichert
 */
public abstract class SteeredMover extends MovingEntity {

	public final MovementController movement;

	public SteeredMover(World world) {
		super(world);
		this.movement = new MovementController(world, this);
	}

	public abstract Steering steering();

	@Override
	public boolean canCrossBorderTo(Direction dir) {
		Tile currentTile = tile(), neighbor = world.neighbor(currentTile, dir);
		return canMoveBetween(currentTile, neighbor);
	}

	/**
	 * Forces this guy to move to the given direction.
	 * 
	 * @param dir direction
	 */
	public void forceMoving(Direction dir) {
		wishDir = dir;
		movement.update();
	}

	/**
	 * Forces this guy to reverse its direction.
	 */
	public void reverseDirection() {
		forceMoving(moveDir.opposite());
	}
}