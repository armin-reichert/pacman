package de.amr.games.pacman.model;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import de.amr.easy.game.math.Vector2f;
import de.amr.games.pacman.model.tiles.Pellet;
import de.amr.games.pacman.model.tiles.Space;
import de.amr.games.pacman.model.tiles.Tile;
import de.amr.games.pacman.model.tiles.Wall;

/**
 * The Pac-Man game world.
 * 
 * @author Armin Reichert
 */
public class Maze {

	static final char WALL = '#';
	static final char SPACE = ' ';
	static final char PELLET = '.';

	static final String[] MAP = {
	/*@formatter:off*/
	"############################", 
	"############################", 
	"############################", 
	"############################", 
	"#............##............#", 
	"#.####.#####.##.#####.####.#", 
	"#.####.#####.##.#####.####.#", 
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
	"#...##.......  .......##...#", 
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

	private final Tile[][] map;
	private final Set<Tile> intersections = new HashSet<>();
	private final Set<Tile> noUpIntersections = new HashSet<>();

	public final int numRows;
	public final int numCols;
	public final int totalFoodCount;

	public final Tile pacManHome;
	public final Tile ghostHouseSeats[] = new Tile[4];
	public final Direction ghostHouseSeatDir[] = new Direction[4];
	public final Tile bonusTile;
	public final Tile cornerNW, cornerNE, cornerSW, cornerSE;
	public final Tile horizonNE, horizonNW, horizonSE, horizonSW;
	public final Tile portalLeft, portalRight;
	public final Tile ghostHouseDoorLeft, ghostHouseDoorRight;
	public final Tile energizers[] = new Tile[4];

	public Maze() {
		numRows = MAP.length;
		numCols = MAP[0].length();
		map = new Tile[numCols][numRows];
		int foodCount = 0;
		for (int row = 0; row < numRows; ++row) {
			for (int col = 0; col < numCols; ++col) {
				char c = MAP[row].charAt(col);
				switch (c) {
				case SPACE:
					map[col][row] = new Space(col, row);
					break;
				case WALL:
					map[col][row] = new Wall(col, row);
					break;
				case PELLET:
					map[col][row] = new Pellet(col, row);
					foodCount += 1;
					break;
				default:
					throw new IllegalArgumentException("Unknown tile content: " + c);
				}
			}
		}
		totalFoodCount = foodCount;

		energizers[0] = map[1][6];
		energizers[1] = map[1][26];
		energizers[2] = map[26][6];
		energizers[3] = map[26][26];
		for (Tile tile : energizers) {
			((Pellet) tile).energizer = true;
		}

		ghostHouseDoorLeft = map[13][15];
		ghostHouseDoorRight = map[14][15];

		ghostHouseSeats[0] = map[13][14];
		ghostHouseSeats[1] = map[11][17];
		ghostHouseSeats[2] = map[13][17];
		ghostHouseSeats[3] = map[15][17];

		ghostHouseSeatDir[0] = Direction.LEFT;
		ghostHouseSeatDir[1] = Direction.UP;
		ghostHouseSeatDir[2] = Direction.DOWN;
		ghostHouseSeatDir[3] = Direction.UP;

		pacManHome = map[13][26];
		bonusTile = map[13][20];

		portalLeft = new Space(-1, 17);
		portalRight = new Space(28, 17);

		// Scattering targets
		horizonNW = map[2][0];
		horizonNE = map[25][0];
		horizonSW = map[0][35];
		horizonSE = map[27][35];

		// Corners inside maze
		cornerNW = map[1][4];
		cornerNE = map[26][4];
		cornerSW = map[1][32];
		cornerSE = map[26][32];

		intersections.addAll(Arrays.asList(
		//@formatter:off
			map[6][4],   map[21][4],
			map[1][8],   map[6][8],	  map[9][8],   map[12][8], map[15][8], map[18][8], map[21][8], map[26][8],
			map[6][11],	 map[21][11],
			map[12][14], map[15][14],
			map[6][17],  map[9][17],	map[18][17], map[21][17],
			map[9][20],	 map[18][20],
			map[6][23],	 map[9][23],	map[18][23], map[21][23],
			map[6][26],	 map[9][26],	map[12][26], map[15][26],	map[18][26], map[21][26],	
			map[3][29],	 map[24][29],
			map[12][32], map[15][32]
		//@formatter:on
		));

		noUpIntersections.addAll(Arrays.asList(map[12][14], map[15][14], map[12][26], map[15][26]));
	}

	public Stream<Tile> tiles() {
		return Arrays.stream(map).flatMap(Arrays::stream);
	}

	/**
	 * Returns the tile at the given tile position. This is either a tile inside the
	 * board, a portal tile or a wall outside.
	 * 
	 * @param col column index
	 * @param row row index
	 * @return the tile with the given coordinates.
	 */
	public Tile tileAt(int col, int row) {
		if (insideBoard(col, row)) {
			return map[col][row];
		}
		if (portalLeft.col == col && portalLeft.row == row) {
			return portalLeft;
		}
		if (portalRight.col == col && portalRight.row == row) {
			return portalRight;
		}
		return new Wall(col, row);
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
		return tileAt(tile.col + n * v.roundedX(), tile.row + n * v.roundedY());
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

	public boolean insideBoard(int col, int row) {
		return 0 <= col && col < numCols && 0 <= row && row < numRows;
	}

	public boolean insideBoard(Tile tile) {
		return insideBoard(tile.col, tile.row);
	}

	public boolean isIntersection(Tile tile) {
		return intersections.contains(tile);
	}

	public boolean isNoUpIntersection(Tile tile) {
		return noUpIntersections.contains(tile);
	}

	public boolean insideGhostHouse(Tile tile) {
		return partOfGhostHouse(tile) && isSpace(tile);
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

	public boolean isSpace(Tile tile) {
		return tile instanceof Space;
	}

	public boolean isWall(Tile tile) {
		return tile instanceof Wall;
	}

	public boolean isTunnel(Tile tile) {
		return tile.row == 17 && (-1 <= tile.col && tile.col <= 5 || tile.col >= 22 && tile.col <= 28);
	}

	public boolean isDoor(Tile tile) {
		return tile == ghostHouseDoorLeft || tile == ghostHouseDoorRight;
	}

	public boolean isNormalPellet(Tile tile) {
		if (tile instanceof Pellet) {
			Pellet pellet = (Pellet) tile;
			return !pellet.energizer && !pellet.eaten;
		}
		return false;
	}

	public boolean isEatenNormalPellet(Tile tile) {
		if (tile instanceof Pellet) {
			Pellet pellet = (Pellet) tile;
			return !pellet.energizer && pellet.eaten;
		}
		return false;
	}

	public boolean isEnergizer(Tile tile) {
		if (tile instanceof Pellet) {
			Pellet pellet = (Pellet) tile;
			return pellet.energizer && !pellet.eaten;
		}
		return false;
	}

	public boolean isEatenEnergizer(Tile tile) {
		if (tile instanceof Pellet) {
			Pellet pellet = (Pellet) tile;
			return pellet.energizer && pellet.eaten;
		}
		return false;
	}

	public void removeFood(Tile tile) {
		if (tile instanceof Pellet) {
			((Pellet) tile).eaten = true;
		}
	}

	public void restoreFood(Tile tile) {
		if (tile instanceof Pellet) {
			((Pellet) tile).eaten = false;
		}
	}
}