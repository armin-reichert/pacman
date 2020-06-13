package de.amr.games.pacman.model;

import de.amr.easy.game.math.Vector2f;

/**
 * A ghost seat has a dedicated start direction which is the initial move direction of the ghost
 * jumping up and down at that seat.
 * 
 * @author Armin Reichert
 */
public class GhostSeat {

	public final Tile tile;
	public final Vector2f position;
	public final Direction startDir;

	public GhostSeat(int col, int row, Direction dir) {
		tile = new Tile(col, row);
		position = Vector2f.of(tile.centerX(), tile.y());
		startDir = dir;
	}
}