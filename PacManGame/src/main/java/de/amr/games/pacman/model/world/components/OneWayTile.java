package de.amr.games.pacman.model.world.components;

import de.amr.games.pacman.model.world.api.Direction;
import de.amr.games.pacman.model.world.api.Tile;

/**
 * A tile that can only be traversed in a certain direction.
 * 
 * @author Armin Reichert
 */
public class OneWayTile {

	public final Tile tile;
	public final Direction dir;

	public OneWayTile(int col, int row, Direction dir) {
		this.tile = Tile.at(col, row);
		this.dir = dir;
	}
}