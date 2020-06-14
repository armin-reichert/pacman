package de.amr.games.pacman.model;

import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import de.amr.easy.game.math.Vector2f;

/**
 * The Pac-Man game world.
 * 
 * @author Armin Reichert
 */
public class Maze {

	private static Tile tile(int col, int row) {
		return new Tile(col, row);
	}

	/*
	 * Map information (content and structural) is stored in 6 bit positions. For example, 48 (binary
	 * 110000) is an intersection where chasing ghosts can only move downwards.
	 */

	/** Tile represents a wall. */
	static final byte B_WALL = 0;

	/** Tile contains (eaten or uneaten) food. */
	static final byte B_FOOD = 1;

	/** Tile contains (eaten or uneaten) energizer. */
	static final byte B_ENERGIZER = 2;

	/** Tile contains eaten food. */
	static final byte B_EATEN = 3;

	/** Tile represents an intersection point. */
	static final byte B_INTERSECTION = 4;

	/** Tile represents a one-way road downwards. */
	static final byte B_ONLY_DOWN = 5;

	byte[][] map = {
		//@formatter:off
		{ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
		{ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
		{ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
		{ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
		{ 1, 2, 2, 2, 2, 2,18, 2, 2, 2, 2, 2, 2, 1, 1, 2, 2, 2, 2, 2, 2,18, 2, 2, 2, 2, 2, 1 },
		{ 1, 2, 1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 2, 1, 1, 2, 1, 1, 1, 1, 1, 2, 1, 1, 1, 1, 2, 1 },
		{ 1, 6, 1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 2, 1, 1, 2, 1, 1, 1, 1, 1, 2, 1, 1, 1, 1, 6, 1 },
		{ 1, 2, 1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 2, 1, 1, 2, 1, 1, 1, 1, 1, 2, 1, 1, 1, 1, 2, 1 },
		{ 1,18, 2, 2, 2, 2,18, 2, 2,18, 2, 2,18, 2, 2,18, 2, 2,18, 2, 2,18, 2, 2, 2, 2,18, 1 },
		{ 1, 2, 1, 1, 1, 1, 2, 1, 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 2, 1, 1, 2, 1, 1, 1, 1, 2, 1 },
		{ 1, 2, 1, 1, 1, 1, 2, 1, 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 2, 1, 1, 2, 1, 1, 1, 1, 2, 1 },
		{ 1, 2, 2, 2, 2, 2,18, 1, 1, 2, 2, 2, 2, 1, 1, 2, 2, 2, 2, 1, 1,18, 2, 2, 2, 2, 2, 1 },
		{ 1, 1, 1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 0, 1, 1, 0, 1, 1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 1 },
		{ 1, 1, 1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 0, 1, 1, 0, 1, 1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 1 },
		{ 1, 1, 1, 1, 1, 1, 2, 1, 1, 0, 0, 0,48, 0, 0,48, 0, 0, 0, 1, 1, 2, 1, 1, 1, 1, 1, 1 },
		{ 1, 1, 1, 1, 1, 1, 2, 1, 1, 0, 1, 1, 1, 0, 0, 1, 1, 1, 0, 1, 1, 2, 1, 1, 1, 1, 1, 1 },
		{ 1, 1, 1, 1, 1, 1, 2, 1, 1, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 1, 1, 2, 1, 1, 1, 1, 1, 1 },
		{ 0, 0, 0, 0, 0, 0,18, 0, 0,16, 1, 0, 0, 0, 0, 0, 0, 1,16, 0, 0,18, 0, 0, 0, 0, 0, 0 },
		{ 1, 1, 1, 1, 1, 1, 2, 1, 1, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 1, 1, 2, 1, 1, 1, 1, 1, 1 },
		{ 1, 1, 1, 1, 1, 1, 2, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 2, 1, 1, 1, 1, 1, 1 },
		{ 1, 1, 1, 1, 1, 1, 2, 1, 1,16, 0, 0, 0, 0, 0, 0, 0, 0,16, 1, 1, 2, 1, 1, 1, 1, 1, 1 },
		{ 1, 1, 1, 1, 1, 1, 2, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 2, 1, 1, 1, 1, 1, 1 },
		{ 1, 1, 1, 1, 1, 1, 2, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 2, 1, 1, 1, 1, 1, 1 },
		{ 1, 2, 2, 2, 2, 2,18, 2, 2,18, 2, 2, 2, 1, 1, 2, 2, 2,18, 2, 2,18, 2, 2, 2, 2, 2, 1 },
		{ 1, 2, 1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 2, 1, 1, 2, 1, 1, 1, 1, 1, 2, 1, 1, 1, 1, 2, 1 },
		{ 1, 2, 1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 2, 1, 1, 2, 1, 1, 1, 1, 1, 2, 1, 1, 1, 1, 2, 1 },
		{ 1, 6, 2, 2, 1, 1,18, 2, 2,18, 2, 2,50, 0, 0,50, 2, 2,18, 2, 2,18, 1, 1, 2, 2, 6, 1 },
		{ 1, 1, 1, 2, 1, 1, 2, 1, 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 2, 1, 1, 2, 1, 1, 2, 1, 1, 1 },
		{ 1, 1, 1, 2, 1, 1, 2, 1, 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 2, 1, 1, 2, 1, 1, 2, 1, 1, 1 },
		{ 1, 2, 2,18, 2, 2, 2, 1, 1, 2, 2, 2, 2, 1, 1, 2, 2, 2, 2, 1, 1, 2, 2, 2,18, 2, 2, 1 },
		{ 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 1, 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 1 },
		{ 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 1, 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 1 },
		{ 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,18, 2, 2,18, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1 },
		{ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
		{ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
		{ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 }
		//@formatter:on
	};

	public final int numRows = 36;
	public final int numCols = 28;
	public final int totalFoodCount;

	public final Seat pacManSeat;
	public final Seat ghostSeats[];
	public final Seat bonusSeat;

	public final Tile portalLeft, portalRight;
	public final Tile horizonNE, horizonNW, horizonSE, horizonSW;
	public final Tile ghostHouseDoorLeft, ghostHouseDoorRight;

	// bit operations

	private boolean on(Tile t, byte bit) {
		return insideBoard(t) && off(t.row, t.col, bit);
	}

	private boolean off(int row, int col, byte bit) {
		return (map[row][col] & (1 << bit)) != 0;
	}

	private void set(int row, int col, byte bit, boolean value) {
		if (value) {
			map[row][col] |= (1 << bit);
		} else {
			map[row][col] &= ~(1 << bit);
		}
	}

	public Maze() {

		totalFoodCount = (int) playingArea().filter(tile -> containsSimplePellet(tile) || containsEnergizer(tile)).count();

		portalLeft = tile(-1, 17);
		portalRight = tile(28, 17);

		ghostHouseDoorLeft = tile(13, 15);
		ghostHouseDoorRight = tile(14, 15);

		//@formatter:off
		ghostSeats = new Seat[] { 
				new Seat(13, 14, Direction.LEFT), 
				new Seat(11, 17, Direction.UP),
				new Seat(13, 17, Direction.DOWN),
				new Seat(15, 17, Direction.UP),
				};
		//@formatter:on

		pacManSeat = new Seat(13, 26, Direction.RIGHT);
		bonusSeat = new Seat(13, 20, null);

		// (unreachable) scattering targets
		horizonNW = tile(2, 0);
		horizonNE = tile(25, 0);
		horizonSW = tile(0, 35);
		horizonSE = tile(27, 35);
	}

	/**
	 * @return stream of tiles of the playing area (omitting wall areas used for the scores)
	 */
	public Stream<Tile> playingArea() {
		return IntStream.range(4 * numCols, 33 * numCols).mapToObj(i -> tile(i % numCols, i / numCols));
	}

	/**
	 * @param tile reference tile
	 * @param dir  some direction
	 * @param n    number of tiles
	 * @return the tile located <code>n</code> tiles away from the reference tile towards the given
	 *         direction. This can be a tile outside of the board!
	 */
	public Tile tileToDir(Tile tile, Direction dir, int n) {
		if (tile.equals(portalLeft) && dir == Direction.LEFT) {
			return portalRight;
		}
		if (tile.equals(portalRight) && dir == Direction.RIGHT) {
			return portalLeft;
		}
		Vector2f v = dir.vector();
		return tile(tile.col + n * v.roundedX(), tile.row + n * v.roundedY());
	}

	/**
	 * @param tile reference tile
	 * @param dir  some direction
	 * @return neighbor towards the given direction. This can be a tile outside of the board!
	 */
	public Tile neighbor(Tile tile, Direction dir) {
		return tileToDir(tile, dir, 1);
	}

	public Optional<Direction> direction(Tile t1, Tile t2) {
		Vector2f v = Vector2f.of(t2.col - t1.col, t2.row - t1.row);
		return Direction.dirs().filter(dir -> dir.vector().equals(v)).findFirst();
	}

	public boolean insideBoard(Tile tile) {
		return 0 <= tile.col && tile.col < numCols && 0 <= tile.row && tile.row < numRows;
	}

	public boolean isIntersection(Tile tile) {
		return on(tile, B_INTERSECTION);
	}

	public boolean isUpwardsBlocked(Tile tile) {
		return on(tile, B_ONLY_DOWN);
	}

	public boolean insideGhostHouse(Tile tile) {
		return isDoor(tile) || 16 <= tile.row && tile.row <= 18 && 11 <= tile.col && tile.col <= 16;
	}

	public boolean atGhostHouseDoor(Tile tile) {
		return isDoor(neighbor(tile, Direction.DOWN));
	}

	public boolean isWall(Tile tile) {
		if (tile.equals(portalLeft) || tile.equals(portalRight)) {
			return false;
		}
		return !insideBoard(tile) || on(tile, B_WALL);
	}

	public boolean isTunnel(Tile tile) {
		return tile.row == 17 && (-1 <= tile.col && tile.col <= 5 || tile.col >= 22 && tile.col <= 28);
	}

	public boolean isDoor(Tile tile) {
		return tile.equals(ghostHouseDoorLeft) || tile.equals(ghostHouseDoorRight);
	}

	public boolean containsSimplePellet(Tile tile) {
		return on(tile, B_FOOD) && !on(tile, B_EATEN) && !on(tile, B_ENERGIZER);
	}

	public boolean containsEnergizer(Tile tile) {
		return on(tile, B_ENERGIZER) && !on(tile, B_EATEN);
	}

	public boolean containsEatenFood(Tile tile) {
		return on(tile, B_FOOD) && on(tile, B_EATEN);
	}

	public void eatFood(Tile tile) {
		if (on(tile, B_FOOD)) {
			set(tile.row, tile.col, B_EATEN, true);
		}
	}

	public void removeFood() {
		for (int row = 0; row < numRows; ++row) {
			for (int col = 0; col < numCols; ++col) {
				if (off(row, col, B_FOOD)) {
					set(row, col, B_EATEN, true);
				}
			}
		}
	}

	public void restoreFood() {
		for (int row = 0; row < numRows; ++row) {
			for (int col = 0; col < numCols; ++col) {
				if (off(row, col, B_FOOD)) {
					set(row, col, B_EATEN, false);
				}
			}
		}
	}

	// these corner positions are not needed in the original game. The algorithm to select a safe corner
	// for ghosts escaping Pac-Man uses these positions.

	public Tile cornerNW() {
		return tile(1, 4);
	}

	public Tile cornerNE() {
		return tile(26, 4);
	}

	public Tile cornerSW() {
		return tile(1, 32);
	}

	public Tile cornerSE() {
		return tile(26, 32);
	}
}