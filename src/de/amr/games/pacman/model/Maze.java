package de.amr.games.pacman.model;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.amr.easy.graph.api.GraphTraversal;
import de.amr.easy.graph.api.UndirectedEdge;
import de.amr.easy.graph.impl.traversal.BreadthFirstTraversal;
import de.amr.easy.grid.api.GridGraph2D;
import de.amr.easy.grid.api.Topology;
import de.amr.easy.grid.impl.GridGraph;
import de.amr.easy.grid.impl.Top4;

/**
 * The original Pac-Man maze.
 * 
 * <p>
 * It is represented by a (grid) graph which may store content and can be used by path finding
 * algorithms.
 * 
 * @author Armin Reichert
 * 
 * @see GridGraph2D
 */
public class Maze {

	public static final Topology NESW = new Top4();

	private static final char WALL = '#';
	private static final char DOOR = 'D';
	private static final char TUNNEL = 'T';
	private static final char SPACE = ' ';

	private static final char PELLET = '.';
	private static final char ENERGIZER = '*';
	private static final char EATEN = ':';

	private static final char POS_BONUS = '$';
	private static final char POS_PACMAN = 'O';
	private static final char POS_BLINKY = 'B';
	private static final char POS_INKY = 'I';
	private static final char POS_PINKY = 'P';
	private static final char POS_CLYDE = 'C';
	private static final char POS_ENDMARKER = ')';

	private final String[] map;
	private final GridGraph<Character, Integer> graph;
	private Tile pacManHome;
	private Tile blinkyHome;
	private Tile pinkyHome;
	private Tile inkyHome;
	private Tile clydeHome;
	private Tile bonusTile;
	private int tunnelRow;
	private int foodTotal;

	public Maze(String mapText) {
		map = mapText.split("\n");
		int numCols = map[0].length(), numRows = map.length;
		for (int row = 0; row < numRows; ++row) {
			for (int col = 0; col < numCols; ++col) {
				char c = map(row, col);
				if (c == POS_BLINKY) {
					blinkyHome = new Tile(col, row);
				} else if (c == POS_PINKY) {
					pinkyHome = new Tile(col, row);
				} else if (c == POS_INKY) {
					inkyHome = new Tile(col, row);
				} else if (c == POS_CLYDE) {
					clydeHome = new Tile(col, row);
				} else if (c == POS_BONUS) {
					bonusTile = new Tile(col, row);
				} else if (c == POS_PACMAN) {
					pacManHome = new Tile(col, row);
				} else if (c == TUNNEL) {
					tunnelRow = row;
				} else if (c == PELLET || c == ENERGIZER) {
					foodTotal += 1;
				}
			}
		}
		graph = new GridGraph<>(numCols, numRows, NESW, v -> null, (u, v) -> 1, UndirectedEdge::new);
		graph.setDefaultVertexLabel(v -> map(graph.row(v), graph.col(v)));
		graph.fill();
		// remove all edges into walls
		graph.edges().filter(edge -> {
			int u = edge.either(), v = edge.other();
			return map(graph.row(u), graph.col(u)) == WALL || map(graph.row(v), graph.col(v)) == WALL;
		}).forEach(graph::removeEdge);
	}

	private char map(int row, int col) {
		return map[row].charAt(col);
	}

	public GridGraph2D<Character, Integer> getGraph() {
		return graph;
	}

	public int numCols() {
		return graph.numCols();
	}

	public int numRows() {
		return graph.numRows();
	}

	public int getTeleportLength() {
		return 4;
	}

	public Stream<Tile> tiles() {
		return graph.vertices().mapToObj(this::tile);
	}

	private boolean isValidTile(Tile tile) {
		return graph.isValidCol(tile.col) && graph.isValidRow(tile.row);
	}

	public Tile getTopLeftCorner() {
		return new Tile(1, 4);
	}

	public Tile getTopRightCorner() {
		return new Tile(numCols() - 2, 4);
	}

	public Tile getBottomLeftCorner() {
		return new Tile(1, numRows() - 4);
	}

	public Tile getBottomRightCorner() {
		return new Tile(numCols() - 2, numRows() - 4);
	}

	public Tile getPacManHome() {
		return pacManHome;
	}

	public Tile getBlinkyHome() {
		return blinkyHome;
	}

	public Tile getPinkyHome() {
		return pinkyHome;
	}

	public Tile getInkyHome() {
		return inkyHome;
	}

	public Tile getClydeHome() {
		return clydeHome;
	}

	public Tile getBonusTile() {
		return bonusTile;
	}

	public int getTunnelRow() {
		return tunnelRow;
	}

	private char getContent(Tile tile) {
		if (inTeleportSpace(tile)) {
			return SPACE;
		}
		return graph.get(cell(tile));
	}

	public boolean inTeleportSpace(Tile tile) {
		return !isValidTile(tile);
	}

	public boolean isTunnel(Tile tile) {
		return getContent(tile) == TUNNEL;
	}

	public boolean isWall(Tile tile) {
		return getContent(tile) == WALL;
	}

	public boolean isDoor(Tile tile) {
		return getContent(tile) == DOOR;
	}

	public boolean isIntersection(Tile tile) {
		int cell = cell(tile);
		if (graph.degree(cell) < 3) {
			return false;
		}
		// exceptions:
		if (graph.get(cell) == DOOR || inGhostHouse(tile)) {
			return false;
		}
		int leftNb = graph.neighbor(cell, Top4.W).getAsInt();
		int rightNb = graph.neighbor(cell, Top4.E).getAsInt();
		if (rightNb == cell(pacManHome) || rightNb == cell(blinkyHome)
				|| graph.get(leftNb) == POS_ENDMARKER) {
			return false;
		}
		return true;
	}

	public boolean inGhostHouse(Tile tile) {
		return Math.abs(tile.row - inkyHome.row) <= 1 && tile.col >= inkyHome.col
				&& tile.col <= clydeHome.col + 1;
	}

	public boolean isPellet(Tile tile) {
		return getContent(tile) == PELLET;
	}

	public boolean isEnergizer(Tile tile) {
		return getContent(tile) == ENERGIZER;
	}

	public boolean isFood(Tile tile) {
		return isPellet(tile) || isEnergizer(tile);
	}

	public boolean isEatenFood(Tile tile) {
		return getContent(tile) == EATEN;
	}

	public void resetFood() {
		graph.clearVertexLabels();
	}

	public int getFoodTotal() {
		return foodTotal;
	}

	void hideFood(Tile tile) {
		graph.set(cell(tile), EATEN);
	}

	public OptionalInt direction(Tile t1, Tile t2) {
		return graph.direction(cell(t1), cell(t2));
	}

	public Optional<Tile> neighborTile(Tile tile, int dir) {
		OptionalInt neighbor = graph.neighbor(cell(tile), dir);
		return neighbor.isPresent() ? Optional.of(tile(neighbor.getAsInt())) : Optional.empty();
	}

	public Stream<Tile> getAdjacentTiles(Tile tile) {
		return graph.adj(cell(tile)).mapToObj(this::tile);
	}

	public List<Tile> findPath(Tile source, Tile target) {
		if (isValidTile(source) && isValidTile(target)) {
			// GraphTraversal pathfinder = new AStarTraversal<>(graph, edge -> 1, graph::manhattan);
			GraphTraversal pathfinder = new BreadthFirstTraversal<>(graph);
			pathfinder.traverseGraph(cell(source), cell(target));
			return pathfinder.path(cell(target)).stream().map(this::tile).collect(Collectors.toList());
		}
		return Collections.emptyList();
	}

	public int euclidean2(Tile t1, Tile t2) {
		return graph.euclidean2(cell(t1), cell(t2));
	}

	public OptionalInt alongPath(List<Tile> path) {
		return path.size() < 2 ? OptionalInt.empty() : direction(path.get(0), path.get(1));
	}

	public int cell(Tile tile) {
		return graph.cell(tile.col, tile.row);
	}

	public Tile tile(int cell) {
		return new Tile(graph.col(cell), graph.row(cell));
	}
}