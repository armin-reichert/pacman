package de.amr.games.pacman.model;

import static de.amr.games.pacman.model.Tile.ENERGIZER;
import static de.amr.games.pacman.model.Tile.PELLET;
import static de.amr.games.pacman.model.Tile.SPACE;
import static de.amr.games.pacman.model.Tile.TUNNEL;
import static de.amr.games.pacman.model.Tile.WALL;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The maze, a 2-dimensional grid of tiles.
 * 
 * @author Armin Reichert
 */
public class Maze {

	public final int numRows;
	public final int numCols;
	public final Tile[][] tiles;
	public Tile pacManHome;
	public Tile ghostHouseSeats[] = new Tile[4];
	public Tile bonusTile;
	public Tile cornerNW, cornerNE, cornerSW, cornerSE;
	public Tile horizonNE, horizonNW, horizonSE, horizonSW;
	public Tile tunnelExitLeft, tunnelExitRight;
	public Tile doorLeft, doorRight;
	public Tile energizers[] = new Tile[4];
	public int totalNumPellets;

	private final Set<Tile> intersections;

	public Maze(String[] map) {
		numRows = map.length;
		numCols = map[0].length();
		tiles = new Tile[numCols][numRows];
		int energizerCount = 0;
		for (int row = 0; row < numRows; ++row) {
			for (int col = 0; col < numCols; ++col) {
				char c = map[row].charAt(col);
				Tile tile = tiles[col][row] = new Tile((byte) col, (byte) row, c);
				if (Character.isDigit(c)) {
					int seat = Integer.valueOf(String.valueOf(c));
					ghostHouseSeats[seat] = tile;
					tile.content = SPACE;
				}
				switch (c) {
				case PELLET:
					totalNumPellets += 1;
					break;
				case ENERGIZER:
					totalNumPellets += 1;
					energizers[energizerCount++] = tile;
					break;
				case 'P':
					pacManHome = tile;
					tile.content = SPACE;
					break;
				case '$':
					bonusTile = tile;
					tile.content = SPACE;
					break;
				default:
					break;
				}
			}
		}

		// Ghost house
		doorLeft = tiles[13][15];
		doorLeft.content = Tile.DOOR;
		doorRight = tiles[14][15];
		doorRight.content = Tile.DOOR;

		tunnelExitLeft = tiles[0][17];
		tunnelExitRight = tiles[27][17];

		// Scattering targets
		horizonNW = tiles[2][0];
		horizonNE = tiles[25][0];
		horizonSW = tiles[0][35];
		horizonSE = tiles[27][35];

		// Corners inside maze
		cornerNW = tiles[1][4];
		cornerNE = tiles[26][4];
		cornerSW = tiles[1][32];
		cornerSE = tiles[26][32];

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
				.filter(neighbor -> !neighbor.isWall() && !neighbor.isDoor())
				.count();
		/*@formatter:on*/
	}

	public Stream<Tile> tiles() {
		return Arrays.stream(tiles).flatMap(Arrays::stream);
	}

	/**
	 * @param col
	 *              a column index
	 * @param row
	 *              a row index
	 * @return the tile with the given coordinates. Tiles outside of the board are either tunnel tiles
	 *         (if in the same row than the board tunnel tiles) or walls otherwise.
	 */
	public Tile tileAt(int col, int row) {
		return insideBoard(col, row) ? tiles[col][row]
				: new Tile((byte) col, (byte) row, row == tunnelExitLeft.row ? TUNNEL : WALL);
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
		return tileAt(tile.col + n * dir.dx, tile.row + n * dir.dy);
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

	public boolean inFrontOfGhostHouseDoor(Tile tile) {
		return tileToDir(tile, Direction.DOWN).isDoor();
	}

	public Optional<Direction> directionBetween(Tile t1, Tile t2) {
		int dx = t2.col - t1.col, dy = t2.row - t1.row;
		return Direction.dirs().filter(dir -> dir.dx == dx && dir.dy == dy).findFirst();
	}

	public Optional<Direction> alongPath(List<Tile> path) {
		return path.size() < 2 ? Optional.empty() : directionBetween(path.get(0), path.get(1));
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
		return tile == tiles[12][14] || tile == tiles[12][26] || tile == tiles[15][14] || tile == tiles[15][26];
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
				sb.append(tiles[col][row].content);
			}
			sb.append("\n");
		}
		return sb.toString();
	}
}