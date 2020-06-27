package de.amr.games.pacman.model.map;

import java.util.List;

import de.amr.games.pacman.model.world.Door;
import de.amr.games.pacman.model.world.Portal;
import de.amr.games.pacman.model.world.Seat;
import de.amr.games.pacman.model.world.Tile;

/**
 * Map represented by a 2D byte array.
 * 
 * @author Armin Reichert
 */
public abstract class GameMap {

	private byte[][] data;

	public final int numCols;
	public final int numRows;

	public GameMap(byte[][] data) {
		this.data = data;
		numRows = data.length;
		numCols = data[0].length;
	}

	public abstract List<Seat> ghostSeats();

	public abstract Seat pacManSeat();

	public abstract Tile bonusTile();

	public abstract List<Door> ghostHouseDoors();

	public abstract List<Portal> portals();

	public boolean contains(int row, int col) {
		return 0 <= row && row < numRows && 0 <= col && col < numCols;
	}

	// bit fiddling

	public boolean is1(int row, int col, byte bit) {
		return (data[row][col] & (1 << bit)) != 0;
	}

	public boolean is0(int row, int col, byte bit) {
		return (data[row][col] & (1 << bit)) == 0;
	}

	public void set0(int row, int col, byte bit) {
		data[row][col] &= ~(1 << bit);
	}

	public void set1(int row, int col, byte bit) {
		data[row][col] |= (1 << bit);
	}
}