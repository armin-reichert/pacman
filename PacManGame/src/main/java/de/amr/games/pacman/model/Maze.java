package de.amr.games.pacman.model;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import de.amr.easy.game.math.Vector2f;

/**
 * The Pac-Man game world.
 * 
 * <p>
 * Information (content and structural) is stored in 6 bit positions. For example, 48 (binary
 * 110000) is an intersection where chasing ghosts can only move downwards.
 * 
 * @author Armin Reichert
 */
public class Maze {

	//@formatter:off
	static final byte BM_WALL         = 0b000001;
	static final byte BM_FOOD         = 0b000010;
	static final byte BM_ENERGIZER    = 0b000100;
	static final byte BM_EATEN        = 0b001000;
	static final byte BM_INTERSECTION = 0b010000;
	static final byte BM_ONE_WAY_DOWN = 0b100000;
	//@formatter:on

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
	public final Tile cornerNW, cornerNE, cornerSW, cornerSE;

	// bit operations

	private boolean is(int row, int col, byte mask) {
		return (map[row][col] & mask) != 0;
	}

	private boolean is(Tile tile, byte mask) {
		return insideMap(tile) && is(tile.row, tile.col, mask);
	}

	private boolean not(Tile tile, byte mask) {
		return !is(tile, mask);
	}

	private void clr(int row, int col, byte mask) {
		map[row][col] &= ~mask;
	}

	private void set(int row, int col, byte mask) {
		map[row][col] |= mask;
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

		// only used by algorithm to calculate routes to "safe" corner for fleeing ghosts
		cornerNW = Tile.at(1, 4);
		cornerNE = Tile.at(26, 4);
		cornerSW = Tile.at(1, 32);
		cornerSE = Tile.at(26, 32);

		int foodCount = 0;
		for (int row = arenaTopRow; row <= arenaBottomRow; ++row) {
			for (int col = 0; col < numCols; ++col) {
				if (is(row, col, BM_FOOD)) {
					++foodCount;
				}
			}
		}
		totalFoodCount = foodCount;
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
		return is(tile, BM_INTERSECTION);
	}

	public boolean isOneWayDown(Tile tile) {
		return is(tile, BM_ONE_WAY_DOWN);
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
		return !insideMap(tile) || is(tile, BM_WALL);
	}

	public boolean isTunnel(Tile tile) {
		return tile.row == 17 && (tile.inCols(-1, 5) || tile.inCols(22, 28));
	}

	public boolean isDoor(Tile tile) {
		return tile.equals(ghostHouseDoorLeft) || tile.equals(ghostHouseDoorRight);
	}

	public boolean containsSimplePellet(Tile tile) {
		return is(tile, BM_FOOD) && not(tile, BM_ENERGIZER) && not(tile, BM_EATEN);
	}

	public boolean containsEnergizer(Tile tile) {
		return is(tile, BM_ENERGIZER) && not(tile, BM_EATEN);
	}

	public boolean containsEatenFood(Tile tile) {
		return is(tile, BM_EATEN) && is(tile, BM_FOOD);
	}

	public void eatFood(Tile tile) {
		set(tile.row, tile.col, BM_EATEN);
	}

	public void eatAllFood() {
		for (int row = arenaTopRow; row <= arenaBottomRow; ++row) {
			for (int col = 0; col < numCols; ++col) {
				if (is(row, col, BM_FOOD)) {
					set(row, col, BM_EATEN);
				}
			}
		}
	}

	public void restoreAllFood() {
		for (int row = arenaTopRow; row <= arenaBottomRow; ++row) {
			for (int col = 0; col < numCols; ++col) {
				if (is(row, col, BM_FOOD)) {
					clr(row, col, BM_EATEN);
				}
			}
		}
	}
}