package de.amr.games.pacman.model;

/**
 * The Pac-Man game world consists of an unbounded grid of tiles.
 * 
 * @author Armin Reichert
 */
public abstract class Tile {

	/** Tile size in pixels. */
	public static final byte SIZE = 8;

	public final byte col;
	public final byte row;

	protected Tile(int col, int row) {
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
	 * @param tile
	 *               other tile
	 * @return straight line distance in tiles (squared).
	 */
	public int distSq(Tile tile) {
		int dx = col - tile.col, dy = row - tile.row;
		return dx * dx + dy * dy;
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
}