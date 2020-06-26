package de.amr.games.pacman.model;

/**
 * Stores the game map information.
 * 
 * @author Armin Reichert
 */
public class GameMap {

	//@formatter:off
	static final byte BM_WALL         = 0b00000001;
	static final byte BM_FOOD         = 0b00000010;
	static final byte BM_ENERGIZER    = 0b00000100;
	static final byte BM_EATEN        = 0b00001000;
	static final byte BM_INTERSECTION = 0b00010000;
	static final byte BM_ONE_WAY_DOWN = 0b00100000;
	static final byte BM_TUNNEL       = 0b01000000;
	//@formatter:on

	private byte[][] data;

	public final int numCols;
	public final int numRows;

	public GameMap(byte[][] data) {
		this.data = data;
		numRows = data.length;
		numCols = data[0].length;
	}

	// bit operations

	private boolean is(int row, int col, byte mask) {
		return (data[row][col] & mask) != 0;
	}

	private boolean not(int row, int col, byte mask) {
		return !is(row, col, mask);
	}

	private void clr(int row, int col, byte mask) {
		data[row][col] &= ~mask;
	}

	private void set(int row, int col, byte mask) {
		data[row][col] |= mask;
	}

	private boolean inRange(int i, int min, int max) {
		return min <= i && i <= max;
	}
	// API

	public boolean contains(int row, int col) {
		return inRange(col, 0, numCols - 1) && inRange(row, 0, numRows - 1);
	}

	public boolean isWall(int row, int col) {
		return is(row, col, BM_WALL);
	}

	public boolean isFood(int row, int col) {
		return is(row, col, BM_FOOD);
	}

	public boolean isEatenFood(int row, int col) {
		return is(row, col, BM_EATEN);
	}

	public boolean isEnergizer(int row, int col) {
		return is(row, col, BM_ENERGIZER);
	}

	public boolean isIntersection(int row, int col) {
		return is(row, col, BM_INTERSECTION);
	}

	public boolean isOneWayDown(int row, int col) {
		return is(row, col, BM_ONE_WAY_DOWN);
	}

	public boolean isTunnel(int row, int col) {
		return is(row, col, BM_TUNNEL);
	}

	public boolean containsEnergizer(int row, int col) {
		return is(row, col, BM_ENERGIZER) && not(row, col, BM_EATEN);
	}

	public boolean containsFood(int row, int col) {
		return not(row, col, BM_EATEN) && is(row, col, BM_FOOD);
	}

	public boolean containsEatenFood(int row, int col) {
		return is(row, col, BM_EATEN) && is(row, col, BM_FOOD);
	}

	public void eatFood(int row, int col) {
		set(row, col, BM_EATEN);
	}

	public void restoreFood(int row, int col) {
		clr(row, col, BM_EATEN);
	}
}