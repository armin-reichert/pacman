package de.amr.games.pacman.model;

import de.amr.easy.game.math.Vector2f;

/**
 * A seat is a maze position with a start direction which is the initial move direction of the
 * creature residing at that seat.
 * 
 * @author Armin Reichert
 */
public class Seat {

	public int number;
	public final Tile tile;
	public final Vector2f position;
	public final Direction startDir;

	public Seat(int number, int col, int row, Direction dir) {
		this.number = number;
		tile = Tile.at(col, row);
		position = Vector2f.of(tile.centerX(), tile.y());
		startDir = dir;
	}
}