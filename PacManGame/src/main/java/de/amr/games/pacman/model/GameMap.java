package de.amr.games.pacman.model;

/**
 * Map represented by a 2D byte array.
 * 
 * @author Armin Reichert
 */
public class GameMap {

	//@formatter:off
	static final byte B_WALL         = 1<<0;
	static final byte B_FOOD         = 1<<1;
	static final byte B_ENERGIZER    = 1<<2;
	static final byte B_EATEN        = 1<<3;
	static final byte B_INTERSECTION = 1<<4;
	static final byte B_ONE_WAY_DOWN = 1<<5;
	static final byte B_TUNNEL       = 1<<6;
	//@formatter:on

	private byte[][] data;

	public final int numCols;
	public final int numRows;

	public GameMap(byte[][] data) {
		this.data = data;
		numRows = data.length;
		numCols = data[0].length;
	}

	public boolean is1(int row, int col, byte bit) {
		return (data[row][col] & bit) != 0;
	}

	public boolean is0(int row, int col, byte bit) {
		return (data[row][col] & bit) == 0;
	}

	public void set0(int row, int col, byte bit) {
		data[row][col] &= ~bit;
	}

	public void set1(int row, int col, byte bit) {
		data[row][col] |= bit;
	}

	public boolean inRange(int row, int col) {
		return 0 <= col && col < numCols && 0 <= row && row < numRows;
	}
}