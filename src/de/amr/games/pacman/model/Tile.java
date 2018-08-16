package de.amr.games.pacman.model;

/**
 * A tile represents a position inside a tile-based world.
 * 
 * @author Armin Reichert
 */
public class Tile {

	public final int col;
	public final int row;
	public final float xOffset;
	public final float yOffset;

	public Tile(int col, int row, float xOffset, float yOffset) {
		this.col = col;
		this.row = row;
		this.xOffset = xOffset;
		this.yOffset = yOffset;
	}
	
	public Tile(int col, int row) {
		this(col, row, 0, 0);
	}

	@Override
	public int hashCode() {
		int sum = col + row;
		return sum * (sum + 1) / 2 + col;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Tile) {
			Tile other = (Tile) obj;
			return col == other.col && row == other.row;
		}
		return super.equals(obj);
	}

	@Override
	public String toString() {
		if (xOffset != 0 || yOffset != 0) {
			return String.format("(%d+%.2f, %d+%.2f)", col, xOffset, row, yOffset);
		}
		return String.format("(%d,%d)", col, row);
	}
}