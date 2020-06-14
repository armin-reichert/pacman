package de.amr.games.pacman.model;

import java.util.Objects;
import java.util.Optional;

import de.amr.easy.game.math.Vector2f;

/**
 * The Pac-Man game world is a grid of tiles.
 * 
 * @author Armin Reichert
 */
public final class Tile {

	/** Tile size in pixels. */
	public static final byte SIZE = 8;

	public static Tile at(int col, int row) {
		return new Tile(col, row);
	}

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
	 * @param other other tile
	 * @return Euclidean distance measured in tiles
	 */
	public double distance(Tile other) {
		int dx = col - other.col, dy = row - other.row;
		return Math.sqrt(dx * dx + dy * dy);
	}

	public boolean inCols(int colMin, int colMax) {
		return colMin <= col && col <= colMax;
	}

	public boolean inRows(int rowMin, int rowMax) {
		return rowMin <= row && row <= rowMax;
	}

	public Optional<Direction> dirTo(Tile other) {
		Vector2f v = Vector2f.of(other.col - col, other.row - row);
		return Direction.dirs().filter(dir -> dir.vector().equals(v)).findFirst();
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