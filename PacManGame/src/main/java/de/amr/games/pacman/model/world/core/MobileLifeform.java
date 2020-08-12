package de.amr.games.pacman.model.world.core;

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
	public void placeAt(Tile tile, float dx, float dy) {
		Tile oldTile = tile();
		tf.setPosition(tile.x() + dx, tile.y() + dy);
		enteredNewTile = !tile().equals(oldTile);
	}
}