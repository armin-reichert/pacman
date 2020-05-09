package de.amr.games.pacman.model;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import de.amr.easy.game.math.Vector2f;

/**
 * The Pac-Man game world.
 * 
 * @author Armin Reichert
 */
public class Maze {

	static final String[] FLOORPLAN = {
	/*@formatter:off*/
	"############################", 
	"############################", 
	"############################", 
	"############################", 
	"#............##............#", 
	"#.####.#####.##.#####.####.#", 
	"#e####.#####.##.#####.####e#", 
	"#.####.#####.##.#####.####.#", 
	"#..........................#", 
	"#.####.##.########.##.####.#", 
	"#.####.##.########.##.####.#", 
	"#......##....##....##......#", 
	"######.##### ## #####.######", 
	"######.##### ## #####.######", 
	"######.##          ##.######", 
	"######.## ###  ### ##.######", 
	"######.## #      # ##.######", 
	"      .   #      #   .      ", 
	"######.## #      # ##.######", 
	"######.## ######## ##.######", 
	"######.##          ##.######", 
	"######.## ######## ##.######", 
	"######.## ######## ##.######", 
	"#............##............#", 
	"#.####.#####.##.#####.####.#", 
	"#.####.#####.##.#####.####.#", 
	"#e..##.......  .......##..e#", 
	"###.##.##.########.##.##.###", 
	"###.##.##.########.##.##.###", 
	"#......##....##....##......#", 
	"#.##########.##.##########.#", 
	"#.##########.##.##########.#", 
	"#..........................#", 
	"############################", 
	"############################", 
	"############################"}; 
	/*@formatter:on*/

	// bitmasks
	static final byte WALL = 1 << 0;
	static final byte FOOD = 1 << 1; // simple pellet or energizer
	static final byte EATEN = 1 << 2;

	private final byte[][] content;

	public final int numRows = 36;
	public final int numCols = 28;
	public final int totalFoodCount;

	public final Set<Tile> intersections;
	public final Set<Tile> noUpIntersections;
	public final Set<Tile> energizerTiles;
	public final Tile pacManHome;
	public final Tile ghostHouseSeats[];
	public final Direction ghostHouseSeatDirs[];
	public final Tile ghostHouseEntry;
	public final Tile portalLeft, portalRight;
	public final Tile bonusTile;
	public final Tile cornerNW, cornerNE, cornerSW, cornerSE;
	public final Tile horizonNE, horizonNW, horizonSE, horizonSW;
	public final Tile ghostHouseDoorLeft, ghostHouseDoorRight;

	private boolean isSet(int col, int row, byte mask) {
		return (content[col][row] & mask) != 0;
	}

	private void set(int col, int row, byte mask) {
		content[col][row] |= mask;
	}

	private void clear(int col, int row, byte mask) {
		content[col][row] &= ~mask;
	}

	public Maze() {
		portalLeft = new Tile(-1, 17);
		portalRight = new Tile(28, 17);

		intersections = new HashSet<>(Arrays.asList(new Tile(6, 4), new Tile(21, 4), new Tile(1, 8), new Tile(6, 8),
				new Tile(9, 8), new Tile(12, 8), new Tile(15, 8), new Tile(18, 8), new Tile(21, 8), new Tile(26, 8),
				new Tile(6, 11), new Tile(21, 11), new Tile(12, 14), new Tile(15, 14), new Tile(6, 17), new Tile(9, 17),
				new Tile(18, 17), new Tile(21, 17), new Tile(9, 20), new Tile(18, 20), new Tile(6, 23), new Tile(9, 23),
				new Tile(18, 23), new Tile(21, 23), new Tile(6, 26), new Tile(9, 26), new Tile(12, 26), new Tile(15, 26),
				new Tile(18, 26), new Tile(21, 26), new Tile(3, 29), new Tile(24, 29), new Tile(12, 32), new Tile(15, 32)));

		noUpIntersections = new HashSet<>(
				Arrays.asList(new Tile(12, 14), new Tile(15, 14), new Tile(12, 26), new Tile(15, 26)));

		ghostHouseEntry = new Tile(13, 14);
		ghostHouseDoorLeft = new Tile(13, 15);
		ghostHouseDoorRight = new Tile(14, 15);

		ghostHouseSeats = new Tile[] { new Tile(13, 14), new Tile(11, 17), new Tile(13, 17), new Tile(15, 17) };
		ghostHouseSeatDirs = new Direction[] { Direction.LEFT, Direction.UP, Direction.DOWN, Direction.UP };

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

		int foodCount = 0;
		energizerTiles = new HashSet<>();
		content = new byte[numCols][numRows];
		for (int row = 0; row < numRows; ++row) {
			for (int col = 0; col < numCols; ++col) {
				char c = FLOORPLAN[row].charAt(col);
				switch (c) {
				case '#':
					set(col, row, WALL);
					break;
				case '.':
				case 'e':
					set(col, row, FOOD);
					if (c == 'e') {
						energizerTiles.add(new Tile(col, row));
					}
					foodCount += 1;
					break;
				default:
					break;
				}
			}
		}
		totalFoodCount = foodCount;
	}

	/**
	 * @return stream of tiles inside the board (no portal tiles)
	 */
	public Stream<Tile> tiles() {
		return IntStream.range(0, numRows * numCols).mapToObj(i -> new Tile(i % numCols, i / numCols));
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
	public Tile tileToDir(Tile tile, Direction dir) {
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
		return intersections.contains(tile);
	}

	public boolean isNoUpIntersection(Tile tile) {
		return noUpIntersections.contains(tile);
	}

	public boolean insideGhostHouse(Tile tile) {
		return partOfGhostHouse(tile) && !isWall(tile);
	}

	public boolean partOfGhostHouse(Tile tile) {
		return 15 <= tile.row && tile.row <= 19 && 10 <= tile.col && tile.col <= 17;
	}

	public boolean atGhostHouseDoor(Tile tile) {
		return isDoor(tileToDir(tile, Direction.DOWN));
	}

	public Vector2f seatPosition(int seat) {
		return Vector2f.of(ghostHouseSeats[seat].centerX(), ghostHouseSeats[seat].y());
	}

	public boolean isWall(Tile tile) {
		if (tile.equals(portalLeft) || tile.equals(portalRight)) {
			return false;
		}
		if (!insideBoard(tile)) {
			return true;
		}
		return isSet(tile.col, tile.row, WALL);
	}

	public boolean isTunnel(Tile tile) {
		return tile.row == 17 && (-1 <= tile.col && tile.col <= 5 || tile.col >= 22 && tile.col <= 28);
	}

	public boolean isDoor(Tile tile) {
		return tile.equals(ghostHouseDoorLeft) || tile.equals(ghostHouseDoorRight);
	}

	public boolean isSimplePellet(Tile tile) {
		return insideBoard(tile) && !energizerTiles.contains(tile) && isSet(tile.col, tile.row, FOOD)
				&& !isSet(tile.col, tile.row, EATEN);
	}

	public boolean isEnergizer(Tile tile) {
		return energizerTiles.contains(tile) && !isSet(tile.col, tile.row, EATEN);
	}

	public boolean isEatenEnergizer(Tile tile) {
		return energizerTiles.contains(tile) && isSet(tile.col, tile.row, EATEN);
	}

	public boolean isEatenSimplePellet(Tile tile) {
		return insideBoard(tile) && isSet(tile.col, tile.row, FOOD) && isSet(tile.col, tile.row, EATEN);
	}

	public void removeFood(Tile tile) {
		if (isSimplePellet(tile) || isEnergizer(tile)) {
			set(tile.col, tile.row, EATEN);
		}
	}

	public void restoreFood(Tile tile) {
		if (isEatenSimplePellet(tile) || isEatenEnergizer(tile)) {
			clear(tile.col, tile.row, EATEN);
		}
	}
}