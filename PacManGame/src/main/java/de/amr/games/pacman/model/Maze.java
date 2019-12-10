package de.amr.games.pacman.model;

import static de.amr.easy.game.Application.LOGGER;
import static de.amr.graph.grid.impl.Grid4Topology.S;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.amr.graph.core.api.UndirectedEdge;
import de.amr.graph.grid.api.GridGraph2D;
import de.amr.graph.grid.impl.Grid4Topology;
import de.amr.graph.grid.impl.GridGraph;
import de.amr.graph.pathfinder.api.GraphSearch;
import de.amr.graph.pathfinder.api.Path;
import de.amr.graph.pathfinder.impl.AStarSearch;

/**
 * The original Pac-Man maze.
 * 
 * <p>
 * The maze is a 2-dimensional grid of tiles, each tile contains a character representing its
 * content. Additionally, a (grid) graph structure is used to allow running path finders on the
 * graph.
 * 
 * @author Armin Reichert
 * 
 * @see GridGraph2D
 */
public class Maze {

	/** Tile size in pixels. */
	public static final int TS = 8;

	/** 4-direction topology (NORTH, EAST, SOUTH, WEST) */
	public static final Grid4Topology NESW = Grid4Topology.get();

	public static final int COLS = 28, ROWS = 36;

	private static final String[] MAP = {
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
	"######.##    B     ##.######", 
	"######.## ###--### ##.######", 
	"######.## #      # ##.######", 
	"tttttt.   #I P C #   .tttttt", 
	"######.## #      # ##.######", 
	"######.## ######## ##.######", 
	"######.##    $     ##.######", 
	"######.## ######## ##.######", 
	"######.## ######## ##.######", 
	"#............##............#", 
	"#.####.#####.##.#####.####.#", 
	"#.####.#####.##.#####.####.#", 
	"#*..##.......O .......##..*#", 
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

	public static final char WALL = '#', DOOR = '-', TUNNEL = 't', SPACE = ' ', PELLET = '.', ENERGIZER = '*',
			EATEN_PELLET = ':', EATEN_ENERGIZER = '~';

	public final GridGraph2D<Tile, Void> graph;

	public final Tile topLeft, topRight, bottomLeft, bottomRight, blinkyScatter, pinkyScatter, inkyScatter,
			clydeScatter, tunnelLeftExit, tunnelRightExit, ghostRevival;

	public /* final */ Tile pacManHome, blinkyHome, inkyHome, pinkyHome, clydeHome, bonusTile;

	public final int totalNumPellets;

	private final Tile[][] board = new Tile[COLS][ROWS];
	private final Set<Tile> intersections;
	private final Set<Tile> energizers = new HashSet<>();

	public Maze() {
		int numPellets = 0;
		for (byte row = 0; row < ROWS; ++row) {
			for (byte col = 0; col < COLS; ++col) {
				char content = MAP[row].charAt(col);
				Tile tile = board[col][row] = new Tile(col, row, content);
				switch (content) {
				case PELLET:
					numPellets += 1;
					break;
				case ENERGIZER:
					numPellets += 1;
					energizers.add(tile);
					break;
				case 'O':
					pacManHome = tile;
					tile.content = SPACE;
					break;
				case 'B':
					blinkyHome = tile;
					tile.content = SPACE;
					break;
				case 'P':
					pinkyHome = tile;
					tile.content = SPACE;
					break;
				case 'I':
					inkyHome = tile;
					tile.content = SPACE;
					break;
				case 'C':
					clydeHome = tile;
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

		totalNumPellets = numPellets;

		tunnelLeftExit = board[0][17];
		tunnelRightExit = board[27][17];

		ghostRevival = board[13][17];

		// Scattering targets
		pinkyScatter = board[2][0];
		blinkyScatter = board[25][0];
		clydeScatter = board[0][35];
		inkyScatter = board[27][35];

		// Corners inside maze
		topLeft = board[1][4];
		topRight = board[26][4];
		bottomLeft = board[1][32];
		bottomRight = board[26][32];

		// Graph where each vertex holds a reference to the corresponding tile
		graph = new GridGraph<>(COLS, ROWS, NESW, this::tile, (u, v) -> null, UndirectedEdge::new);
		graph.fill();
		//@formatter:off
		graph.edges()
			.filter(edge -> isWall(tile(edge.either())) || isWall(tile(edge.other())))
			.forEach(graph::removeEdge);

		intersections = graph.vertices()
			.filter(vertex -> graph.degree(vertex) >= 3)
			.mapToObj(this::tile)
			.filter(tile -> !inFrontOfGhostHouseDoor(tile))
			.filter(tile -> !partOfGhostHouse(tile))
			.collect(Collectors.toSet());
		//@formatter:on
	}

	private int vertex(Tile tile) {
		return graph.cell(tile.col, tile.row);
	}

	private Tile tile(int vertex) {
		return board[graph.col(vertex)][graph.row(vertex)];
	}

	public Stream<Tile> tiles() {
		return graph.vertices().mapToObj(this::tile);
	}

	public Stream<Tile> energizerTiles() {
		return energizers.stream();
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
		byte _col = (byte) col, _row = (byte) row;
		return insideBoard(_col, _row) ? board[_col][_row]
				: new Tile(_col, _row, _row == tunnelLeftExit.row ? TUNNEL : WALL);
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
	public Tile tileToDir(Tile tile, byte dir, int n) {
		return tileAt(tile.col + n * NESW.dx(dir), tile.row + n * NESW.dy(dir));
	}

	/**
	 * @param tile
	 *               reference tile
	 * @param dir
	 *               some direction
	 * @return neighbor towards the given direction. This can be a tile outside of the board!
	 */
	public Tile tileToDir(Tile tile, byte dir) {
		return tileToDir(tile, dir, 1);
	}

	public boolean insideBoard(byte col, byte row) {
		return 0 <= col && col < COLS && 0 <= row && row < ROWS;
	}

	public boolean insideBoard(Tile tile) {
		return insideBoard(tile.col, tile.row);
	}

	public boolean isTunnel(Tile tile) {
		return tile.content == TUNNEL;
	}

	public boolean isWall(Tile tile) {
		return tile.content == WALL;
	}

	public boolean isDoor(Tile tile) {
		return tile.content == DOOR;
	}

	public boolean inFrontOfGhostHouseDoor(Tile tile) {
		return isDoor(tileToDir(tile, S));
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
		return tile == board[12][14] || tile == board[12][26] || tile == board[15][14] || tile == board[15][26];
	}

	// food

	public boolean containsFood(Tile tile) {
		return containsPellet(tile) || containsEnergizer(tile);
	}

	public boolean containsPellet(Tile tile) {
		return tile.content == PELLET;
	}

	public boolean containsEnergizer(Tile tile) {
		return tile.content == ENERGIZER;
	}

	public boolean containsEatenFood(Tile tile) {
		return tile.content == EATEN_PELLET || tile.content == EATEN_ENERGIZER;
	}

	public void removeFood(Tile tile) {
		if (tile.content == PELLET) {
			tile.content = EATEN_PELLET;
		}
		else if (tile.content == ENERGIZER) {
			tile.content = EATEN_ENERGIZER;
		}
		else {
			throw new IllegalArgumentException(String.format("Tile %s does not contain food", tile));
		}
	}

	public void restoreFood() {
		tiles().filter(this::containsEatenFood)
				.forEach(tile -> tile.content = tile.content == EATEN_PELLET ? PELLET : ENERGIZER);
	}

	public void removeFood() {
		tiles().filter(this::containsFood).forEach(this::removeFood);
	}

	// navigation and path finding

	public Optional<Byte> direction(Tile t1, Tile t2) {
		int dx = t2.col - t1.col, dy = t2.row - t1.row;
		return NESW.dirs().filter(dir -> NESW.dx(dir) == dx && NESW.dy(dir) == dy).findFirst();
	}

	private int pathFinderCalls;

	public List<Tile> findPath(Tile source, Tile target) {
		if (insideBoard(source) && insideBoard(target)) {
			GraphSearch pathfinder = new AStarSearch(graph, (u, v) -> 1, graph::manhattan);
			Path path = pathfinder.findPath(vertex(source), vertex(target));
			pathFinderCalls += 1;
			if (pathFinderCalls % 100 == 0) {
				LOGGER.info(String.format("%d'th pathfinding executed", pathFinderCalls));
			}
			return path.vertexStream().boxed().map(this::tile).collect(Collectors.toList());
		}
		return Collections.emptyList();
	}

	public Optional<Byte> alongPath(List<Tile> path) {
		return path.size() < 2 ? Optional.empty() : direction(path.get(0), path.get(1));
	}
}