package de.amr.games.pacman.model;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.amr.easy.game.math.Vector2f;
import de.amr.games.pacman.model.tiles.Energizer;
import de.amr.games.pacman.model.tiles.Pellet;
import de.amr.games.pacman.model.tiles.Space;
import de.amr.games.pacman.model.tiles.Tile;
import de.amr.games.pacman.model.tiles.Tunnel;
import de.amr.games.pacman.model.tiles.Wall;

/**
 * The Pac-Man game world.
 * 
 * @author Armin Reichert
 */
public class Maze {

	public static final char WALL = '#';
	public static final char TUNNEL = 't';
	public static final char SPACE = ' ';
	public static final char PELLET = '.';
	public static final char ENERGIZER = '*';

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
	public final Tunnel portalLeft, portalRight;
	public final Tile doorLeft, doorRight;
	public final Energizer energizers[] = new Energizer[4];

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
				switch (c) {
				case SPACE:
					map[col][row] = new Space(col, row);
					break;
				case WALL:
					map[col][row] = new Wall(col, row);
					break;
				case TUNNEL:
					map[col][row] = new Tunnel(col, row);
					break;
				case PELLET:
					map[col][row] = new Pellet(col, row);
					pelletCount += 1;
					break;
				case ENERGIZER:
					Energizer energizer = new Energizer(col, row);
					map[col][row] = energizer;
					energizers[energizerCount++] = energizer;
					pelletCount += 1;
					break;
				default:
					throw new IllegalArgumentException("Unknown tile content: " + c);
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

		portalLeft = new Tunnel(-1, 17);
		portalRight = new Tunnel(28, 17);

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
				.filter(neighbor -> !isWall(neighbor) && !isDoor(neighbor))
				.count();
		/*@formatter:on*/
	}

	public Stream<Tile> tiles() {
		return Arrays.stream(map).flatMap(Arrays::stream);
	}

	/**
	 * Returns the tile at the given tile position. This is either a tile inside the board, a portal
	 * tile or a wall outside. Tiles inside the board and the two portal tiles are created once so
	 * equality can be tested using <code>==</code>. Other tiles are created on-demand and must be
	 * compared using {@link Object#equals(Object)}. For tiles outside of the board, the column and row
	 * index must fit into a byte.
	 * 
	 * @param col
	 *              a column index
	 * @param row
	 *              a row index
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
		Vector2f v = dir.vector();
		return tileAt(tile.col + n * v.roundedX(), tile.row + n * v.roundedY());
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

	public boolean isWall(Tile tile) {
		return tile instanceof Wall;
	}

	public boolean isTunnel(Tile tile) {
		return tile instanceof Tunnel;
	}

	public boolean isSpace(Tile tile) {
		return tile instanceof Space;
	}

	public boolean isPellet(Tile tile) {
		return tile instanceof Pellet && !((Pellet) tile).eaten;
	}

	public boolean isEatenPellet(Tile tile) {
		return tile instanceof Pellet && ((Pellet) tile).eaten;
	}

	public boolean isEnergizer(Tile tile) {
		return tile instanceof Energizer && !((Energizer) tile).eaten;
	}

	public boolean isEatenEnergizer(Tile tile) {
		return tile instanceof Energizer && ((Energizer) tile).eaten;
	}

	public boolean isFood(Tile tile) {
		return isPellet(tile) || isEnergizer(tile);
	}

	public boolean isEatenFood(Tile tile) {
		return isEatenPellet(tile) || isEatenEnergizer(tile);
	}

	public void removeFood(Tile tile) {
		if (isPellet(tile)) {
			((Pellet) tile).eaten = true;
		}
		else if (isEnergizer(tile)) {
			((Energizer) tile).eaten = true;
		}
		else {
			throw new IllegalArgumentException(String.format("Tile %s does not contain food", this));
		}
	}

	public void restoreFood(Tile tile) {
		if (isEatenPellet(tile)) {
			((Pellet) tile).eaten = false;
		}
		else if (isEatenEnergizer(tile)) {
			((Energizer) tile).eaten = false;
		}
		else {
			throw new IllegalArgumentException(String.format("Tile %s does not contain food", this));
		}
	}

	public boolean inFrontOfGhostHouseDoor(Tile tile) {
		return isDoor(tileToDir(tile, Direction.DOWN));
	}

	public Optional<Direction> direction(Tile t1, Tile t2) {
		Vector2f v = Vector2f.of(t2.col - t1.col, t2.row - t1.row);
		return Direction.dirs().filter(dir -> dir.vector().equals(v)).findFirst();
	}

	public Vector2f seatPosition(int seat) {
		return Vector2f.of(ghostHouseSeats[seat].centerX(), ghostHouseSeats[seat].y());
	}

	public boolean partOfGhostHouse(Tile tile) {
		return 15 <= tile.row && tile.row <= 19 && 10 <= tile.col && tile.col <= 17;
	}

	public boolean inGhostHouse(Tile tile) {
		return partOfGhostHouse(tile) && isSpace(tile);
	}

	public boolean isIntersection(Tile tile) {
		return intersections.contains(tile);
	}

	public boolean isNoUpIntersection(Tile tile) {
		return tile == map[12][14] || tile == map[12][26] || tile == map[15][14] || tile == map[15][26];
	}

	public void restoreFood() {
		tiles().filter(this::isEatenFood).forEach(this::restoreFood);
	}

	public void removeFood() {
		tiles().filter(this::isFood).forEach(this::removeFood);
	}
}