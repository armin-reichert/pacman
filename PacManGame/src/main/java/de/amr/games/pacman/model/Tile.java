package de.amr.games.pacman.model;

import static de.amr.games.pacman.model.Maze.NESW;

/**
 * A tile coordinate.
 * 
 * @author Armin Reichert
 */
public class Tile {

	public final int col;
	public final int row;

	public Tile(int col, int row) {
		this.col = col;
		this.row = row;
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
		return String.format("(%d,%d)", col, row);
	}

	/**
	 * @param dir
	 *              some direction
	 * @param n
	 *              number of tiles
	 * @return tile that lies <code>n</code> tiles away from the given tile towards the given direction.
	 *         This can be an invalid tile position.
	 */
	public Tile tileTowards(int dir, int n) {
		if (n < 0) {
			throw new IllegalArgumentException("Number of tiles must not be negative");
		}
		return new Tile(col + n * NESW.dx(dir), row + n * NESW.dy(dir));
	}

	/**
	 * @param dir
	 *              some direction
	 * @return neighbor towards the given direction. This can be an invalid tile position.
	 */
	public Tile tileTowards(int dir) {
		return tileTowards(dir, 1);
	}

}