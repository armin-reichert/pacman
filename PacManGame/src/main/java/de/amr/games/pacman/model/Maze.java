package de.amr.games.pacman.model;

import static de.amr.easy.game.Application.LOGGER;
import static java.lang.Math.abs;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.OptionalInt;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.amr.easy.game.assets.Assets;
import de.amr.graph.core.api.UndirectedEdge;
import de.amr.graph.grid.api.GridGraph2D;
import de.amr.graph.grid.impl.GridGraph;
import de.amr.graph.grid.impl.Top4;
import de.amr.graph.pathfinder.api.GraphSearch;
import de.amr.graph.pathfinder.api.Path;
import de.amr.graph.pathfinder.impl.AStarSearch;

/**
 * The original Pac-Man maze.
 * 
 * <p>
 * The maze is a 2-dimensional grid of tiles, each tile contains a character
 * representing its content. Additionally, a grid graph structure is used to
 * allow running path finders on the graph.
 * 
 * @author Armin Reichert
 * 
 * @see GridGraph2D
 */
public class Maze {

	public static Top4 NESW = Top4.get();

	static final char WALL = '#', DOOR = 'D', TUNNEL = 'T', TELEPORT_L = '<', TELEPORT_R = '>', SPACE = ' ',
			PELLET = '.', ENERGIZER = '*', EATEN = '%';

	private final Tile[][] board;
	private final Set<Tile> unrestricted = new HashSet<>();
	private final Set<Tile> upwardsBlocked = new HashSet<>();
	private final Set<Tile> energizerTiles = new HashSet<>();
	private int pathFinderCalls;

	public int numCols, numRows;
	public int foodTotal;

	public Tile topLeft, topRight, bottomLeft, bottomRight, pacManHome, blinkyHome, blinkyScatter, pinkyHome,
			pinkyScatter, inkyHome, inkyScatter, clydeHome, clydeScatter, bonusTile, teleportLeft, teleportRight,
			ghostRevival;

	public final GridGraph<Tile, Void> gridGraph;

	public Maze() {
		String[] map = Assets.text("maze.txt").split("\n");
		numCols = map[0].length();
		numRows = map.length;
		board = new Tile[numCols][numRows];
		for (int row = 0; row < numRows; ++row) {
			for (int col = 0; col < numCols; ++col) {
				Tile tile = board[col][row] = new Tile(col, row, SPACE);
				switch (map[row].charAt(col)) {
				case WALL:
					tile.content = WALL;
					break;
				case DOOR:
					tile.content = DOOR;
					break;
				case TUNNEL:
					tile.content = TUNNEL;
					break;
				case TELEPORT_L:
					tile.content = TUNNEL;
					teleportLeft = tile;
					break;
				case TELEPORT_R:
					tile.content = TUNNEL;
					teleportRight = tile;
					break;
				case PELLET:
					tile.content = PELLET;
					foodTotal += 1;
					break;
				case ENERGIZER:
					tile.content = ENERGIZER;
					energizerTiles.add(tile);
					foodTotal += 1;
					break;
				case 'O':
					pacManHome = tile;
					break;
				case 'B':
					blinkyHome = tile;
					break;
				case 'P':
					pinkyHome = tile;
					ghostRevival = tile;
					break;
				case 'I':
					inkyHome = tile;
					break;
				case 'C':
					clydeHome = tile;
					break;
				case 'b':
					tile.content = WALL;
					blinkyScatter = tile;
					break;
				case 'p':
					tile.content = WALL;
					pinkyScatter = tile;
					break;
				case 'i':
					tile.content = WALL;
					inkyScatter = tile;
					break;
				case 'c':
					tile.content = WALL;
					clydeScatter = tile;
					break;
				case '$':
					bonusTile = tile;
					break;
				default:
					break;
				}
			}
		}

		// Corners inside maze
		topLeft = board[1][4];
		topRight = board[numCols - 2][4];
		bottomLeft = board[1][numRows - 4];
		bottomRight = board[numCols - 2][numRows - 4];

		// Grid graph structure, vertex content is (reference to) corresponding tile
		gridGraph = new GridGraph<>(numCols, numRows, NESW, this::tile, (u, v) -> null, UndirectedEdge::new);
		gridGraph.fill();

		// Remove edges into walls
		gridGraph.edges()
				.filter(e -> gridGraph.get(e.either()).content == WALL || gridGraph.get(e.other()).content == WALL)
				.forEach(gridGraph::removeEdge);

		// Intersections: unrestricted or upwards-blocked?
		gridGraph.vertices().filter(v -> gridGraph.degree(v) >= 3).mapToObj(this::tile)
		//@formatter:off
			// exclude tiles above ghost house doors:
			.filter(tile -> !isDoor(tileToDir(tile, Top4.S)))
			// exclude doors:
			.filter(tile -> !isDoor(tile))
			// exclude tiles inside ghost house:
			.filter(tile -> !insideGhostHouse(tile))
			// separate "real" intersections:
			.forEach(intersection -> {
				if (intersection == tileToDir(blinkyHome, Top4.W)	
				 || intersection == tileToDir(blinkyHome, Top4.E, 2)
				 || intersection == tileToDir(pacManHome, Top4.W)
				 || intersection == tileToDir(pacManHome, Top4.E, 2)) {
					upwardsBlocked.add(intersection);
				}
				else {
					unrestricted.add(intersection);
				}
			});
		//@formatter:on
	}

	private int vertex(Tile tile) {
		return gridGraph.cell(tile.col, tile.row);
	}

	private Tile tile(int vertex) {
		return board[gridGraph.col(vertex)][gridGraph.row(vertex)];
	}

	public Stream<Tile> tiles() {
		return gridGraph.vertices().mapToObj(this::tile);
	}

	/**
	 * @param col a column index
	 * @param row a row index
	 * @return a board tile or a new (tunnel) tile if the coordinates are outside of
	 *         the board
	 */
	public Tile tileAt(int col, int row) {
		return gridGraph.isValidCol(col) && gridGraph.isValidRow(row) ? board[col][row] : new Tile(col, row, TUNNEL);
	}

	/**
	 * @param tile reference tile
	 * @param dir  some direction
	 * @param n    number of tiles
	 * @return the tile located <code>n</code> tiles away from the reference tile
	 *         towards the given direction. This can be a tile outside of the board!
	 */
	public Tile tileToDir(Tile tile, int dir, int n) {
		return tileAt(tile.col + n * NESW.dx(dir), tile.row + n * NESW.dy(dir));
	}

	/**
	 * @param tile reference tile
	 * @param dir  some direction
	 * @return neighbor towards the given direction. This can be a tile outside of
	 *         the board!
	 */
	public Tile tileToDir(Tile tile, int dir) {
		return tileToDir(tile, dir, 1);
	}

	public boolean insideBoard(Tile tile) {
		return gridGraph.isValidCol(tile.col) && gridGraph.isValidRow(tile.row);
	}

	public boolean insideTunnel(Tile tile) {
		return tile.content == TUNNEL;
	}

	public boolean isWall(Tile tile) {
		return tile.content == WALL;
	}

	public boolean isDoor(Tile tile) {
		return tile.content == DOOR;
	}

	public boolean inFrontOfGhostHouseDoor(Tile tile) {
		return isDoor(tileToDir(tile, Top4.S));
	}

	public boolean insideGhostHouse(Tile tile) {
		return abs(tile.row - inkyHome.row) <= 1 && tile.col >= inkyHome.col && tile.col <= clydeHome.col + 1;
	}

	public boolean isIntersection(Tile tile) {
		return isUnrestrictedIntersection(tile) || isUpwardsBlockedIntersection(tile);
	}

	public boolean isUnrestrictedIntersection(Tile tile) {
		return unrestricted.contains(tile);
	}

	public boolean isUpwardsBlockedIntersection(Tile tile) {
		return upwardsBlocked.contains(tile);
	}

	// food

	public boolean containsPellet(Tile tile) {
		return tile.content == PELLET;
	}

	public boolean containsEnergizer(Tile tile) {
		return tile.content == ENERGIZER;
	}

	public boolean containsFood(Tile tile) {
		return containsPellet(tile) || containsEnergizer(tile);
	}

	public boolean containsEatenFood(Tile tile) {
		return tile.content == EATEN;
	}

	public void restoreFood() {
		tiles().filter(this::containsEatenFood)
				.forEach(tile -> tile.content = energizerTiles.contains(tile) ? ENERGIZER : PELLET);
	}

	public void removeFood() {
		tiles().filter(this::containsFood).forEach(this::removeFood);
	}

	public void removeFood(Tile tile) {
		tile.content = EATEN;
	}

	// navigation

	public OptionalInt direction(Tile t1, Tile t2) {
		return gridGraph.direction(vertex(t1), vertex(t2));
	}

	public List<Tile> findPath(Tile source, Tile target) {
		if (insideBoard(source) && insideBoard(target)) {
			GraphSearch pathfinder = new AStarSearch(gridGraph, (u, v) -> 1, gridGraph::manhattan);
			Path path = pathfinder.findPath(vertex(source), vertex(target));
			pathFinderCalls += 1;
			if (pathFinderCalls % 100 == 0) {
				LOGGER.info(String.format("%d'th pathfinding executed", pathFinderCalls));
			}
			return path.vertexStream().boxed().map(this::tile).collect(Collectors.toList());
		}
		return Collections.emptyList();
	}

	public int manhattanDist(Tile t1, Tile t2) {
		return gridGraph.manhattan(vertex(t1), vertex(t2));
	}

	public OptionalInt alongPath(List<Tile> path) {
		return path.size() < 2 ? OptionalInt.empty() : direction(path.get(0), path.get(1));
	}
}