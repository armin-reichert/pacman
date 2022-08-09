/*
MIT License

Copyright (c) 2019 Armin Reichert

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package de.amr.games.pacmanfsm.lib;

import static de.amr.easy.game.math.V2f.v;

import java.util.Objects;
import java.util.Optional;

import de.amr.easy.game.math.V2f;

/**
 * The Pac-Man game world is layed out into tiles of eight pixels size each.
 * 
 * @author Armin Reichert
 */
public final class Tile {

	/** Tile size in pixels. */
	public static final byte TS = 8;

	/**
	 * @param either either tile
	 * @param other  other tile
	 * @return Euclidean distance measured in tiles
	 */
	public static double euclideanDistance(Tile either, Tile other) {
		int dx = either.col - other.col;
		int dy = either.row - other.row;
		return Math.sqrt(dx * dx + dy * dy);
	}

	/**
	 * @param either either tile
	 * @param other  other tile
	 * @return Manhattan distance measured in tiles
	 */
	public static int manhattanDistance(Tile either, Tile other) {
		int dx = Math.abs(either.col - other.col);
		int dy = Math.abs(either.row - other.row);
		return dx + dy;
	}

	/**
	 * Nicer constructor function.
	 * 
	 * @param col column
	 * @param row row
	 * @return tile at this location
	 */
	public static Tile at(int col, int row) {
		return new Tile(col, row);
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
		return col * TS;
	}

	/**
	 * @return this tiles' y-coordinate
	 */
	public int y() {
		return row * TS;
	}

	/**
	 * @return this tiles' center x-coordinate
	 */
	public int centerX() {
		return col * TS + TS / 2;
	}

	/**
	 * @return this tiles' center y-coordinate
	 */
	public int centerY() {
		return row * TS + TS / 2;
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
		V2f v = v(other.col - col, other.row - row);
		return Direction.dirs().filter(dir -> dir.vector().equals(v)).findFirst();
	}

	/**
	 * @param dir some direction
	 * @return the tile towards the given direction
	 */
	public Tile towards(Direction dir) {
		V2f v = dir.vector();
		return Tile.at(col + (int) v.x(), row + (int) v.y());
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