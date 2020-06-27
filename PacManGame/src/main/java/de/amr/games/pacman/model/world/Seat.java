package de.amr.games.pacman.model.world;

import de.amr.easy.game.math.Vector2f;
import de.amr.games.pacman.model.Direction;
import de.amr.games.pacman.model.Tile;

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
		tile = Tile.col_row(col, row);
		position = Vector2f.of(tile.centerX(), tile.y());
		startDir = dir;
	}
}