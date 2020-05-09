package de.amr.games.pacman.model;

import java.util.Objects;

/**
 * The Pac-Man game world consists of an unbounded grid of tiles.
 * 
 * @author Armin Reichert
 */
public final class Tile {

	/** Tile size in pixels. */
	public static final byte SIZE = 8;

	public final byte col;
	public final byte row;

	public Tile(int col, int row) {
		this.col = (byte) col;
		this.row = (byte) row;
	}

	public int x() {
		return col * SIZE;
	}

	public int y() {
		return row * SIZE;
	}

	public int centerX() {
		return col * SIZE + SIZE / 2;
	}

	public int centerY() {
		return row * SIZE + SIZE / 2;
	}

	/**
	 * @param tile other tile
	 * @return straight line distance in tiles (squared).
	 */
	public int distSq(Tile tile) {
		int dx = col - tile.col, dy = row - tile.row;
		return dx * dx + dy * dy;
	}

	@Override
	public int hashCode() {
		return Objects.hash(col, row);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Tile other = (Tile) obj;
		return col == other.col && row == other.row;
	}

	@Override
	public String toString() {
		return String.format("(%d,%d)", col, row);
	}
}