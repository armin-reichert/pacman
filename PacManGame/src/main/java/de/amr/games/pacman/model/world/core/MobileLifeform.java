package de.amr.games.pacman.model.world.core;

import java.util.stream.Stream;

import de.amr.games.pacman.model.world.api.Direction;
import de.amr.games.pacman.model.world.api.Tile;
import de.amr.games.pacman.model.world.api.World;

/**
 * A life form that can move through the world.
 * 
 * @author Armin Reichert
 */
public class MobileLifeform extends Lifeform {

	public Direction moveDir;
	public Direction wishDir;
	public boolean enteredNewTile;

	public MobileLifeform(World world) {
		super(world);
	}

	@Override
	public void placeAt(Tile tile, float xOffset, float yOffset) {
		Tile oldLocation = tileLocation();
		tf.setPosition(tile.x() + xOffset, tile.y() + yOffset);
		enteredNewTile = !tileLocation().equals(oldLocation);
	}


	/**
	 * @param dirs directions
	 * @return if the lifeform is moving to any of the given directions
	 */
	public boolean isMoving(Direction... dirs) {
		return Stream.of(dirs).anyMatch(dir -> dir == moveDir);
	}
}