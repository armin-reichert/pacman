package de.amr.games.pacman.model;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import de.amr.easy.game.math.Vector2f;

/**
 * The Pac-Man game world.
 * 
 * <p>
 * Map information (content and structural) is stored in 6 bit positions. For example, 48 (binary
 * 110000) is an intersection where chasing ghosts can only move downwards.
 * 
 * @author Armin Reichert
 */
public class Maze {

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
	static final byte B_ONE_WAY_DOWN = 5;

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

	public final int arenaTopRow = 4;
	public final int arenaBottomRow = 32;

	public final int totalFoodCount;

	public final Seat pacManSeat;
	public final Seat ghostSeats[];
	public final Seat bonusSeat;

	public final Tile portalLeft, portalRight;
	public final Tile horizonNE, horizonNW, horizonSE, horizonSW;
	public final Tile ghostHouseDoorLeft, ghostHouseDoorRight;

	// bit operations

	private boolean is_1(int row, int col, byte bit) {
		return (map[row][col] & (1 << bit)) != 0;
	}

	private boolean is_1(Tile tile, byte bit) {
		return insideMap(tile) && is_1(tile.row, tile.col, bit);
	}

	private void set_0(int row, int col, byte bit) {
		map[row][col] &= ~(1 << bit);
	}

	private void set_1(int row, int col, byte bit) {
		map[row][col] |= (1 << bit);
	}

	public Maze() {

		pacManSeat = new Seat(13, 26, Direction.RIGHT);

		//@formatter:off
		ghostSeats = new Seat[] { 
				new Seat(13, 14, Direction.LEFT), 
				new Seat(11, 17, Direction.UP),
				new Seat(13, 17, Direction.DOWN),
				new Seat(15, 17, Direction.UP),
				};
		//@formatter:on

		bonusSeat = new Seat(13, 20, null);

		portalLeft = Tile.at(-1, 17);
		portalRight = Tile.at(28, 17);

		ghostHouseDoorLeft = Tile.at(13, 15);
		ghostHouseDoorRight = Tile.at(14, 15);

		// (unreachable) scattering targets
		horizonNW = Tile.at(2, 0);
		horizonNE = Tile.at(25, 0);
		horizonSW = Tile.at(0, 35);
		horizonSE = Tile.at(27, 35);

		totalFoodCount = (int) arena().filter(tile -> containsSimplePellet(tile) || containsEnergizer(tile)).count();
	}

	public boolean insideMap(Tile tile) {
		return tile.inCols(0, numCols - 1) && tile.inRows(0, numRows - 1);
	}

	/**
	 * @return Tiles comprising the playing area (omitting the areas above and below used for the
	 *         scores)
	 */
	public Stream<Tile> arena() {
		return IntStream.range(arenaTopRow * numCols, (arenaBottomRow + 1) * numCols)
				.mapToObj(i -> Tile.at(i % numCols, i / numCols));
	}

	/**
	 * @param tile reference tile
	 * @param dir  some direction
	 * @param n    number of tiles
	 * @return The tile located <code>n</code> tiles away from the reference tile towards the given
	 *         direction. This can be a tile outside of the map.
	 */
	public Tile tileToDir(Tile tile, Direction dir, int n) {
		if (tile.equals(portalLeft) && dir == Direction.LEFT) {
			return portalRight;
		}
		if (tile.equals(portalRight) && dir == Direction.RIGHT) {
			return portalLeft;
		}
		Vector2f v = dir.vector();
		return Tile.at(tile.col + n * v.roundedX(), tile.row + n * v.roundedY());
	}

	/**
	 * @param tile reference tile
	 * @param dir  some direction
	 * @return Neighbor towards the given direction. This can be a tile outside of the map.
	 */
	public Tile neighbor(Tile tile, Direction dir) {
		return tileToDir(tile, dir, 1);
	}

	public boolean isIntersection(Tile tile) {
		return is_1(tile, B_INTERSECTION);
	}

	public boolean isOneWayDown(Tile tile) {
		return is_1(tile, B_ONE_WAY_DOWN);
	}

	public boolean insideGhostHouse(Tile tile) {
		return isDoor(tile) || tile.inCols(11, 16) && tile.inRows(16, 18);
	}

	public boolean atGhostHouseDoor(Tile tile) {
		return isDoor(neighbor(tile, Direction.DOWN));
	}

	public boolean isWall(Tile tile) {
		if (tile.equals(portalLeft) || tile.equals(portalRight)) {
			return false;
		}
		return !insideMap(tile) || is_1(tile, B_WALL);
	}

	public boolean isTunnel(Tile tile) {
		return tile.row == 17 && (tile.inCols(-1, 5) || tile.inCols(22, 28));
	}

	public boolean isDoor(Tile tile) {
		return tile.equals(ghostHouseDoorLeft) || tile.equals(ghostHouseDoorRight);
	}

	public boolean containsSimplePellet(Tile tile) {
		return is_1(tile, B_FOOD) && !is_1(tile, B_EATEN) && !is_1(tile, B_ENERGIZER);
	}

	public boolean containsEnergizer(Tile tile) {
		return is_1(tile, B_ENERGIZER) && !is_1(tile, B_EATEN);
	}

	public boolean containsEatenFood(Tile tile) {
		return is_1(tile, B_FOOD) && is_1(tile, B_EATEN);
	}

	public void eatFood(Tile tile) {
		if (is_1(tile, B_FOOD)) {
			set_1(tile.row, tile.col, B_EATEN);
		}
	}

	public void removeFood() {
		for (int row = arenaTopRow; row <= arenaBottomRow; ++row) {
			for (int col = 0; col < numCols; ++col) {
				if (is_1(row, col, B_FOOD)) {
					set_1(row, col, B_EATEN);
				}
			}
		}
	}

	public void restoreFood() {
		for (int row = arenaTopRow; row <= arenaBottomRow; ++row) {
			for (int col = 0; col < numCols; ++col) {
				if (is_1(row, col, B_FOOD)) {
					set_0(row, col, B_EATEN);
				}
			}
		}
	}

	/*
	 * These inner corner positions are not needed in the original game. The algorithm to select a safe
	 * corner for ghosts escaping Pac-Man uses them.
	 */

	public Tile cornerNW() {
		return Tile.at(1, 4);
	}

	public Tile cornerNE() {
		return Tile.at(26, 4);
	}

	public Tile cornerSW() {
		return Tile.at(1, 32);
	}

	public Tile cornerSE() {
		return Tile.at(26, 32);
	}
}