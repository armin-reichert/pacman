package de.amr.games.pacman.controller.creatures;

import de.amr.easy.game.controller.Lifecycle;
import de.amr.games.pacman.controller.steering.api.Steering;
import de.amr.games.pacman.controller.steering.common.MovementController;
import de.amr.games.pacman.model.world.api.Direction;
import de.amr.games.pacman.model.world.api.World;
import de.amr.games.pacman.model.world.components.Tile;
import de.amr.games.pacman.model.world.core.TileWorldEntity;

/**
 * Guys can move through the world in a controlled way.
 * 
 * @author Armin Reichert
 */
public abstract class Guy extends TileWorldEntity implements Lifecycle {

	public final String name;
	public final MovementController movement;
	public Direction moveDir;
	public Direction wishDir;
	public boolean enteredNewTile;

	public Guy(World world, String name) {
		super(world);
		this.name = name;
		this.movement = new MovementController(world, this);
	}

	public abstract Steering steering();

	public abstract boolean canMoveBetween(Tile currentTile, Tile neighbor);

	public abstract float getSpeed();

	public boolean canCrossBorderTo(Direction dir) {
		Tile currentTile = tile(), neighbor = world.neighbor(currentTile, dir);
		return canMoveBetween(currentTile, neighbor);
	}

	@Override
	public void placeAt(Tile tile, float dx, float dy) {
		Tile oldTile = tile();
		super.placeAt(tile, dx, dy);
		enteredNewTile = !tile().equals(oldTile);
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