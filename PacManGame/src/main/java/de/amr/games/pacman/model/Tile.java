package de.amr.games.pacman.model;

/**
 * The Pac-Man game world consists of an unbounded grid of tiles. The board
 * tiles are created exactly once when the board/maze is created. Therefore
 * tiles inside the board can be compared by identity where in the general case,
 * tiles have to be compared using equals(). The tile content is not relevant
 * when comparing tiles.
 * 
 * @author Armin Reichert
 */
public class Tile {

	/** Tile size in pixels. */
	public static final byte SIZE = 8;

	public static final char WALL = '#';
	public static final char TUNNEL = 't';
	public static final char SPACE = ' ';
	public static final char PELLET = '.';
	public static final char ENERGIZER = '*';
	public static final char EATEN_PELLET = ':';
	public static final char EATEN_ENERGIZER = '~';

	public final byte col;
	public final byte row;
	public char content;

	Tile(byte col, byte row, char content) {
		this.col = col;
		this.row = row;
		this.content = content;
	}

	public boolean isTunnel() {
		return content == TUNNEL;
	}

	public boolean isWall() {
		return content == WALL;
	}

	public boolean containsFood() {
		return containsPellet() || containsEnergizer();
	}

	public boolean containsPellet() {
		return content == PELLET;
	}

	public boolean containsEnergizer() {
		return content == ENERGIZER;
	}

	public boolean containsEatenPellet() {
		return content == EATEN_PELLET;
	}

	public boolean containsEatenEnergizer() {
		return content == EATEN_ENERGIZER;
	}

	public boolean containsEatenFood() {
		return content == EATEN_PELLET || content == EATEN_ENERGIZER;
	}

	public void removeFood() {
		if (containsPellet()) {
			content = EATEN_PELLET;
		} else if (containsEnergizer()) {
			content = EATEN_ENERGIZER;
		} else {
			throw new IllegalArgumentException(String.format("Tile %s does not contain food", this));
		}
	}

	public void restoreFood() {
		if (containsEatenPellet()) {
			content = PELLET;
		} else if (containsEatenEnergizer()) {
			content = ENERGIZER;
		} else {
			throw new IllegalArgumentException(String.format("Tile %s does not contain eaten food", this));
		}
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
		return String.format("(%d,%d,'%c')", col, row, content);
	}
}