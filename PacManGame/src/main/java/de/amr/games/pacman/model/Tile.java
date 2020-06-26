package de.amr.games.pacman.model;

import java.util.Objects;
import java.util.Optional;

import de.amr.easy.game.math.Vector2f;

/**
 * The Pac-Man game world is layed out into tiles of eight pixels size each.
 * 
 * @author Armin Reichert
 */
public final class Tile {

	/** Tile size in pixels. */
	public static final byte SIZE = 8;

	/**
	 * @param either either tile
	 * @param other  other tile
	 * @return Euclidean distance measured in tiles
	 */
	public static double euclideanDistance(Tile either, Tile other) {
		int dx = either.col - other.col, dy = either.row - other.row;
		return Math.sqrt(dx * dx + dy * dy);
	}

	/**
	 * @param either either tile
	 * @param other  other tile
	 * @return Manhattan distance measured in tiles
	 */
	public static int manhattanDistance(Tile either, Tile other) {
		int dx = Math.abs(either.col - other.col), dy = Math.abs(either.row - other.row);
		return dx + dy;
	}

	/**
	 * Nicer constructor function.
	 * 
	 * @param x tile column index
	 * @param y tile row index
	 * @return tile at this location
	 */
	public static Tile xy(int x, int y) {
		return new Tile(x, y);
	}

	/** Tile column index. Left to right, zero based. */
	public final short col;

	/** Tile row index. Top to bottom, zero based. */
	public final short row;

	/**
	 * Constructor function.
	 * 
	 * @param col tile column index
	 * @param row tile row index
	 * @return new tile
	 */
	public Tile(int col, int row) {
		this.col = (short) col;
		this.row = (short) row;
	}

	/**
	 * @return this tiles' x-coordinate
	 */
	public int x() {
		return col * SIZE;
	}

	/**
	 * @return this tiles' y-coordinate
	 */
	public int y() {
		return row * SIZE;
	}

	/**
	 * @return this tiles' center x-coordinate
	 */
	public int centerX() {
		return col * SIZE + SIZE / 2;
	}

	/**
	 * @return this tiles' center y-coordinate
	 */
	public int centerY() {
		return row * SIZE + SIZE / 2;
	}

	/**
	 * @param other other tile
	 * @return Euclidean distance to other tile measured in tiles
	 */
	public double distance(Tile other) {
		return euclideanDistance(this, other);
	}

	/**
	 * @param other other tile
	 * @return Manhattan distance to other tile measured in tiles
	 */
	public int manhattanDistance(Tile other) {
		return manhattanDistance(this, other);
	}

	/**
	 * @param min minimum column index
	 * @param max maximum column index
	 * @return {@code true} if this tile is in the given column index range (boundaries inclusive)
	 */
	public boolean inColumnRange(int min, int max) {
		return min <= col && col <= max;
	}

	/**
	 * @param min minimum row index
	 * @param max maximum row index
	 * @return {@code true} if this tile is in the given row index range (boundaries inclusive)
	 */
	public boolean inRowRange(int min, int max) {
		return min <= row && row <= max;
	}

	/**
	 * @param other other tile
	 * @return the direction towards the other tile, if it is a neighbor tile
	 */
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