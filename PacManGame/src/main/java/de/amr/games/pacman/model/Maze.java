package de.amr.games.pacman.model;

import static de.amr.games.pacman.model.Tile.ENERGIZER;
import static de.amr.games.pacman.model.Tile.PELLET;
import static de.amr.games.pacman.model.Tile.SPACE;
import static de.amr.games.pacman.model.Tile.TUNNEL;
import static de.amr.games.pacman.model.Tile.WALL;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.amr.easy.game.math.Vector2f;

/**
 * The Pac-Man game world.
 * 
 * @author Armin Reichert
 */
public class Maze {

	final String[] data = {
	/*@formatter:off*/
	"############################", 
	"############################", 
	"############################", 
	"############################", 
	"#............##............#", 
	"#.####.#####.##.#####.####.#", 
	"#*####.#####.##.#####.####*#", 
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
	"tttttt.   #      #   .tttttt", 
	"######.## #      # ##.######", 
	"######.## ######## ##.######", 
	"######.##          ##.######", 
	"######.## ######## ##.######", 
	"######.## ######## ##.######", 
	"#............##............#", 
	"#.####.#####.##.#####.####.#", 
	"#.####.#####.##.#####.####.#", 
	"#*..##.......  .......##..*#", 
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

	public final int numRows;
	public final int numCols;
	public final int totalNumPellets;

	public final Tile pacManHome;
	public final Tile ghostHouseSeats[] = new Tile[4];
	public final Tile bonusTile;
	public final Tile cornerNW, cornerNE, cornerSW, cornerSE;
	public final Tile horizonNE, horizonNW, horizonSE, horizonSW;
	public final Tile portalLeft, portalRight;
	public final Tile doorLeft, doorRight;
	public final Tile energizers[] = new Tile[4];

	private final Tile[][] map;
	private final Set<Tile> intersections;

	public Maze() {
		numRows = data.length;
		numCols = data[0].length();
		map = new Tile[numCols][numRows];
		int energizerCount = 0;
		int pelletCount = 0;
		for (int row = 0; row < numRows; ++row) {
			for (int col = 0; col < numCols; ++col) {
				char c = data[row].charAt(col);
				map[col][row] = new Tile((byte) col, (byte) row, c);
				switch (c) {
				case PELLET:
					pelletCount += 1;
					break;
				case ENERGIZER:
					pelletCount += 1;
					energizers[energizerCount++] = map[col][row];
					break;
				default:
					break;
				}
			}
		}
		totalNumPellets = pelletCount;

		// Ghost house
		doorLeft = map[13][15];
		doorRight = map[14][15];
		ghostHouseSeats[0] = map[13][14];
		ghostHouseSeats[1] = map[11][17];
		ghostHouseSeats[2] = map[13][17];
		ghostHouseSeats[3] = map[15][17];

		pacManHome = map[13][26];
		bonusTile = map[13][20];

		portalLeft = new Tile((byte) -1, (byte) 17, TUNNEL);
		portalRight = new Tile((byte) 28, (byte) 17, TUNNEL);

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

		intersections = tiles()
		/*@formatter:off*/
				.filter(tile -> numFreeNeighborTiles(tile) > 2)
				.filter(tile -> !inFrontOfGhostHouseDoor(tile))
				.filter(tile -> !partOfGhostHouse(tile))
				.collect(Collectors.toSet());
		/*@formatter:on*/
	}

	private long numFreeNeighborTiles(Tile tile) {
		/*@formatter:off*/
		return Direction.dirs()
				.map(dir -> tileToDir(tile, dir))
				.filter(this::insideBoard)
				.filter(neighbor -> !neighbor.isWall() && !isDoor(neighbor))
				.count();
		/*@formatter:on*/
	}

	public Stream<Tile> tiles() {
		return Arrays.stream(map).flatMap(Arrays::stream);
	}

	/**
	 * @param col
	 *              a column index
	 * @param row
	 *              a row index
	 * @return the tile with the given coordinates. Tiles outside of the board are tunnel tiles (if in
	 *         the same row as the board tunnel) or walls otherwise.
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
		return new Tile((byte) col, (byte) row, WALL);
	}

	/**
	 * @param tile
	 *               reference tile
	 * @param dir
	 *               some direction
	 * @param n
	 *               number of tiles
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
		Vector2f dirVector = dir.vector();
		return tileAt(tile.col + n * dirVector.roundedX(), tile.row + n * dirVector.roundedY());
	}

	/**
	 * @param tile
	 *               reference tile
	 * @param dir
	 *               some direction
	 * @return neighbor towards the given direction. This can be a tile outside of the board!
	 */
	public Tile tileToDir(Tile tile, Direction dir) {
		return tileToDir(tile, dir, 1);
	}

	public boolean insideBoard(int col, int row) {
		return 0 <= col && col < numCols && 0 <= row && row < numRows;
	}

	public boolean insideBoard(Tile tile) {
		return insideBoard(tile.col, tile.row);
	}

	public boolean isDoor(Tile tile) {
		return tile == doorLeft || tile == doorRight;
	}

	public boolean inFrontOfGhostHouseDoor(Tile tile) {
		return isDoor(tileToDir(tile, Direction.DOWN));
	}

	public boolean isPortal(Tile tile) {
		return tile.equals(portalLeft) || tile.equals(portalRight);
	}

	public Optional<Direction> direction(Tile t1, Tile t2) {
		Vector2f dirVector = Vector2f.of(t2.col - t1.col, t2.row - t1.row);
		return Direction.dirs().filter(dir -> dir.vector().equals(dirVector)).findFirst();
	}

	public boolean partOfGhostHouse(Tile tile) {
		return 15 <= tile.row && tile.row <= 19 && 10 <= tile.col && tile.col <= 17;
	}

	public boolean inGhostHouse(Tile tile) {
		return partOfGhostHouse(tile) && tile.content == SPACE;
	}

	public boolean isIntersection(Tile tile) {
		return intersections.contains(tile);
	}

	public boolean isNoUpIntersection(Tile tile) {
		return tile == map[12][14] || tile == map[12][26] || tile == map[15][14] || tile == map[15][26];
	}

	public void restoreFood() {
		tiles().filter(Tile::containsEatenFood).forEach(Tile::restoreFood);
	}

	public void removeFood() {
		tiles().filter(Tile::containsFood).forEach(Tile::removeFood);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int row = 0; row < numRows; ++row) {
			for (int col = 0; col < numCols; ++col) {
				sb.append(map[col][row].content);
			}
			sb.append("\n");
		}
		return sb.toString();
	}
}