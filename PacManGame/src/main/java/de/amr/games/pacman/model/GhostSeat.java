package de.amr.games.pacman.model;

import de.amr.easy.game.math.Vector2f;

public class GhostSeat {

	public final Tile tile;
	public final Direction exitDir;
	public final Vector2f position;

	public GhostSeat(int col, int row, Direction dir) {
		tile = new Tile(col, row);
		exitDir = dir;
		position = Vector2f.of(tile.centerX(), tile.y());
	}
}
