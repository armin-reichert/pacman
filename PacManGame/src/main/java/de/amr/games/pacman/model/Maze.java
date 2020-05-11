package de.amr.games.pacman.model;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import de.amr.easy.game.math.Vector2f;

/**
 * The Pac-Man game world.
 * 
 * @author Armin Reichert
 */
public class Maze {

	// bits
	static final byte WALL = 0, FOOD = 1, ENERGIZER = 2, EATEN = 3, INTERSECTION = 4, UPWARDS_BLOCKED = 5;

	byte[][] content = {
		//@formatter:off
		{ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
		{ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
		{ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
		{ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
		{ 1, 2, 2, 2, 2, 2, 18, 2, 2, 2, 2, 2, 2, 1, 1, 2, 2, 2, 2, 2, 2, 18, 2, 2, 2, 2, 2, 1 },
		{ 1, 2, 1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 2, 1, 1, 2, 1, 1, 1, 1, 1, 2, 1, 1, 1, 1, 2, 1 },
		{ 1, 6, 1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 2, 1, 1, 2, 1, 1, 1, 1, 1, 2, 1, 1, 1, 1, 6, 1 },
		{ 1, 2, 1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 2, 1, 1, 2, 1, 1, 1, 1, 1, 2, 1, 1, 1, 1, 2, 1 },
		{ 1, 18, 2, 2, 2, 2, 18, 2, 2, 18, 2, 2, 18, 2, 2, 18, 2, 2, 18, 2, 2, 18, 2, 2, 2, 2, 18, 1 },
		{ 1, 2, 1, 1, 1, 1, 2, 1, 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 2, 1, 1, 2, 1, 1, 1, 1, 2, 1 },
		{ 1, 2, 1, 1, 1, 1, 2, 1, 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 2, 1, 1, 2, 1, 1, 1, 1, 2, 1 },
		{ 1, 2, 2, 2, 2, 2, 18, 1, 1, 2, 2, 2, 2, 1, 1, 2, 2, 2, 2, 1, 1, 18, 2, 2, 2, 2, 2, 1 },
		{ 1, 1, 1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 0, 1, 1, 0, 1, 1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 1 },
		{ 1, 1, 1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 0, 1, 1, 0, 1, 1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 1 },
		{ 1, 1, 1, 1, 1, 1, 2, 1, 1, 0, 0, 0, 48, 0, 0, 48, 0, 0, 0, 1, 1, 2, 1, 1, 1, 1, 1, 1 },
		{ 1, 1, 1, 1, 1, 1, 2, 1, 1, 0, 1, 1, 1, 0, 0, 1, 1, 1, 0, 1, 1, 2, 1, 1, 1, 1, 1, 1 },
		{ 1, 1, 1, 1, 1, 1, 2, 1, 1, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 1, 1, 2, 1, 1, 1, 1, 1, 1 },
		{ 0, 0, 0, 0, 0, 0, 18, 0, 0, 16, 1, 0, 0, 0, 0, 0, 0, 1, 16, 0, 0, 18, 0, 0, 0, 0, 0, 0 },
		{ 1, 1, 1, 1, 1, 1, 2, 1, 1, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 1, 1, 2, 1, 1, 1, 1, 1, 1 },
		{ 1, 1, 1, 1, 1, 1, 2, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 2, 1, 1, 1, 1, 1, 1 },
		{ 1, 1, 1, 1, 1, 1, 2, 1, 1, 16, 0, 0, 0, 0, 0, 0, 0, 0, 16, 1, 1, 2, 1, 1, 1, 1, 1, 1 },
		{ 1, 1, 1, 1, 1, 1, 2, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 2, 1, 1, 1, 1, 1, 1 },
		{ 1, 1, 1, 1, 1, 1, 2, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 2, 1, 1, 1, 1, 1, 1 },
		{ 1, 2, 2, 2, 2, 2, 18, 2, 2, 18, 2, 2, 2, 1, 1, 2, 2, 2, 18, 2, 2, 18, 2, 2, 2, 2, 2, 1 },
		{ 1, 2, 1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 2, 1, 1, 2, 1, 1, 1, 1, 1, 2, 1, 1, 1, 1, 2, 1 },
		{ 1, 2, 1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 2, 1, 1, 2, 1, 1, 1, 1, 1, 2, 1, 1, 1, 1, 2, 1 },
		{ 1, 6, 2, 2, 1, 1, 18, 2, 2, 18, 2, 2, 50, 0, 0, 50, 2, 2, 18, 2, 2, 18, 1, 1, 2, 2, 6, 1 },
		{ 1, 1, 1, 2, 1, 1, 2, 1, 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 2, 1, 1, 2, 1, 1, 2, 1, 1, 1 },
		{ 1, 1, 1, 2, 1, 1, 2, 1, 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 2, 1, 1, 2, 1, 1, 2, 1, 1, 1 },
		{ 1, 2, 2, 18, 2, 2, 2, 1, 1, 2, 2, 2, 2, 1, 1, 2, 2, 2, 2, 1, 1, 2, 2, 2, 18, 2, 2, 1 },
		{ 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 1, 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 1 },
		{ 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 1, 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 1 },
		{ 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 18, 2, 2, 18, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1 },
		{ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
		{ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
		{ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 }
		//@formatter:on
	};

	public final int numRows = 36;
	public final int numCols = 28;
	public final int totalFoodCount = 244;

	public final Tile pacManHome;
	public final Tile ghostHome[];
	public final Direction ghostHomeDir[];
	public final Tile ghostHouseEntry;
	public final Tile portalLeft, portalRight;
	public final Tile bonusTile;
	public final Tile cornerNW, cornerNE, cornerSW, cornerSE;
	public final Tile horizonNE, horizonNW, horizonSE, horizonSW;
	public final Tile ghostHouseDoorLeft, ghostHouseDoorRight;

	private final List<Tile> playingAreaTiles = IntStream.range(0, numRows * numCols).filter(i -> {
		int row = i / numCols;
		return row >= 4 && row <= 32;
	}).mapToObj(i -> new Tile(i % numCols, i / numCols)).collect(Collectors.toList());

	private boolean is(Tile t, byte bit) {
		return insideBoard(t) && is(t.row, t.col, bit);
	}

	private boolean is(int row, int col, byte bit) {
		return (content[row][col] & (1 << bit)) != 0;
	}

	private void set(int row, int col, byte bit, boolean value) {
		if (value) {
			content[row][col] |= (1 << bit);
		} else {
			content[row][col] &= ~(1 << bit);
		}
	}

	public Maze() {
		portalLeft = new Tile(-1, 17);
		portalRight = new Tile(28, 17);

		ghostHouseEntry = new Tile(13, 14);
		ghostHouseDoorLeft = new Tile(13, 15);
		ghostHouseDoorRight = new Tile(14, 15);

		ghostHome = new Tile[] { new Tile(13, 14), new Tile(11, 17), new Tile(13, 17), new Tile(15, 17) };
		ghostHomeDir = new Direction[] { Direction.LEFT, Direction.UP, Direction.DOWN, Direction.UP };

		pacManHome = new Tile(13, 26);
		bonusTile = new Tile(13, 20);

		// Scattering targets
		horizonNW = new Tile(2, 0);
		horizonNE = new Tile(25, 0);
		horizonSW = new Tile(0, 35);
		horizonSE = new Tile(27, 35);

		// Corners inside maze
		cornerNW = new Tile(1, 4);
		cornerNE = new Tile(26, 4);
		cornerSW = new Tile(1, 32);
		cornerSE = new Tile(26, 32);
	}

	/**
	 * @return stream of tiles of the playing area (walls above and below playing
	 *         area and portal tiles are omitted)
	 */
	public Stream<Tile> playingArea() {
		return playingAreaTiles.stream();
	}

	/**
	 * @param tile reference tile
	 * @param dir  some direction
	 * @param n    number of tiles
	 * @return the tile located <code>n</code> tiles away from the reference tile
	 *         towards the given direction. This can be a tile outside of the board!
	 */
	public Tile tileToDir(Tile tile, Direction dir, int n) {
		if (tile.equals(portalLeft) && dir == Direction.LEFT) {
			return portalRight;
		}
		if (tile.equals(portalRight) && dir == Direction.RIGHT) {
			return portalLeft;
		}
		Vector2f v = dir.vector();
		return new Tile(tile.col + n * v.roundedX(), tile.row + n * v.roundedY());
	}

	/**
	 * @param tile reference tile
	 * @param dir  some direction
	 * @return neighbor towards the given direction. This can be a tile outside of
	 *         the board!
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
		return is(tile, INTERSECTION);
	}

	public boolean isUpwardsBlocked(Tile tile) {
		return is(tile, UPWARDS_BLOCKED);
	}

	public boolean insideGhostHouse(Tile tile) {
		return isDoor(tile) || 16 <= tile.row && tile.row <= 18 && 11 <= tile.col && tile.col <= 16;
	}

	public boolean atGhostHouseDoor(Tile tile) {
		return isDoor(neighbor(tile, Direction.DOWN));
	}

	public Vector2f seatPosition(int seat) {
		return Vector2f.of(ghostHome[seat].centerX(), ghostHome[seat].y());
	}

	public boolean isWall(Tile tile) {
		if (tile.equals(portalLeft) || tile.equals(portalRight)) {
			return false;
		}
		return !insideBoard(tile) || is(tile, WALL);
	}

	public boolean isTunnel(Tile tile) {
		return tile.row == 17 && (-1 <= tile.col && tile.col <= 5 || tile.col >= 22 && tile.col <= 28);
	}

	public boolean isDoor(Tile tile) {
		return tile.equals(ghostHouseDoorLeft) || tile.equals(ghostHouseDoorRight);
	}

	public boolean isSimplePellet(Tile tile) {
		return is(tile, FOOD) && !is(tile, EATEN) && !is(tile, ENERGIZER);
	}

	public boolean isEnergizer(Tile tile) {
		return is(tile, ENERGIZER) && !is(tile, EATEN);
	}

	public boolean isEatenFood(Tile tile) {
		return is(tile, FOOD) && is(tile, EATEN);
	}

	public void removeFood(Tile tile) {
		if (is(tile, FOOD)) {
			set(tile.row, tile.col, EATEN, true);
		}
	}

	public void removeFood() {
		for (int row = 0; row < numRows; ++row) {
			for (int col = 0; col < numCols; ++col) {
				if (is(row, col, FOOD)) {
					set(row, col, EATEN, true);
				}
			}
		}
	}

	public void restoreFood() {
		for (int row = 0; row < numRows; ++row) {
			for (int col = 0; col < numCols; ++col) {
				if (is(row, col, FOOD)) {
					set(row, col, EATEN, false);
				}
			}
		}
	}
}