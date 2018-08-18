package de.amr.games.pacman.model;

import static de.amr.games.pacman.model.Food.ENERGIZER;
import static de.amr.games.pacman.model.Food.PELLET;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.amr.easy.graph.api.GraphTraversal;
import de.amr.easy.graph.api.UndirectedEdge;
import de.amr.easy.graph.impl.traversal.AStarTraversal;
import de.amr.easy.graph.impl.traversal.BreadthFirstTraversal;
import de.amr.easy.grid.api.GridGraph2D;
import de.amr.easy.grid.api.Topology;
import de.amr.easy.grid.impl.GridGraph;
import de.amr.easy.grid.impl.Top4;

/**
 * The original Pac-Man maze. It is represented by a grid graph which may store content and can be
 * used by path finding algorithms.
 * 
 * @author Armin Reichert
 * 
 * @see GridGraph2D
 * @see AStarTraversal
 */
public class Maze {

	public static final Topology NESW = new Top4();

	// Structure
	private static final char WALL = '#';
	private static final char DOOR = 'D';
	private static final char TUNNEL = 'T';

	// Position markers
	private static final char POS_BONUS = '$';
	private static final char POS_PACMAN = 'O';
	private static final char POS_BLINKY = 'B';
	private static final char POS_INKY = 'I';
	private static final char POS_PINKY = 'P';
	private static final char POS_CLYDE = 'C';
	private static final char POS_ENDMARKER = ')';

	private final String[] originalData;
	private final GridGraph<Character, Integer> graph;
	private Tile pacManHome;
	private Tile blinkyHome;
	private Tile pinkyHome;
	private Tile inkyHome;
	private Tile clydeHome;
	private Tile bonusTile;
	private int tunnelRow;
	private int foodTotal;

	public Maze(String map) {
		originalData = map.split("\n");
		int numCols = originalData[0].length(), numRows = originalData.length;
		for (int row = 0; row < numRows; ++row) {
			for (int col = 0; col < numCols; ++col) {
				char c = originalData(row, col);
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
		graph.setDefaultVertexLabel(v -> originalData(graph.row(v), graph.col(v)));
		graph.fill();
		graph.edges().filter(edge -> {
			int u = edge.either(), v = edge.other();
			return originalData(graph.row(u), graph.col(u)) == WALL
					|| originalData(graph.row(v), graph.col(v)) == WALL;
		}).forEach(graph::removeEdge);
	}

	private char originalData(int row, int col) {
		return originalData[row].charAt(col);
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
		return 6;
	}

	public Stream<Tile> tiles() {
		return graph.vertices().mapToObj(this::tile);
	}

	private boolean isValidTile(Tile tile) {
		return graph.isValidCol(tile.col) && graph.isValidRow(tile.row);
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

	public boolean isTeleportSpace(Tile tile) {
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

	public void resetFood() {
		graph.clearVertexLabels();
	}

	public int getFoodTotal() {
		return foodTotal;
	}

	public char getContent(int col, int row) {
		return graph.get(graph.cell(col, row));
	}

	public char getContent(Tile tile) {
		return isValidTile(tile) ? graph.get(cell(tile)) : ' ';
	}

	public void setEatenFood(Tile tile) {
		graph.set(cell(tile), Food.EATEN);
	}

	public OptionalInt direction(Tile t1, Tile t2) {
		return graph.direction(cell(t1), cell(t2));
	}

	public boolean areAdjacentTiles(Tile t1, Tile t2) {
		return graph.adjacent(cell(t1), cell(t2));
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