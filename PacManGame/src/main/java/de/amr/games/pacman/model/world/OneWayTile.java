package de.amr.games.pacman.model.world;

import de.amr.games.pacman.model.Direction;

public class OneWayTile {

	public final Tile tile;
	public final Direction dir;

	public OneWayTile(int col, int row, Direction dir) {
		this.tile = Tile.col_row(col, row);
		this.dir = dir;
	}
}
