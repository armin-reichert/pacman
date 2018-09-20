package de.amr.games.pacman.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.amr.easy.game.Application;
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

	private static final char BONUS = '$';
	private static final char PACMAN_HOME = 'O';
	private static final char BLINKY_HOME = 'B';
	private static final char BLINKY_ST = 'b';
	private static final char INKY_HOME = 'I';
	private static final char INKY_ST = 'i';
	private static final char PINKY_HOME = 'P';
	private static final char PINKY_ST = 'p';
	private static final char CLYDE_HOME = 'C';
	private static final char CLYDE_ST = 'c';

	private final String[] map;
	private final GridGraph<Character, Void> graph;
	private Tile pacManHome;
	private Tile blinkyHome;
	private Tile pinkyHome;
	private Tile inkyHome;
	private Tile clydeHome;
	private Tile blinkyScatteringTarget;
	private Tile pinkyScatteringTarget;
	private Tile inkyScatteringTarget;
	private Tile clydeScatteringTarget;
	private Tile bonusTile;
	private int tunnelRow;
	private int foodTotal;
	private Set<Tile> freeIntersections = new HashSet<>();
	private Set<Tile> notUpIntersections = new HashSet<>();

	private long pathFinderCalls;

	public Maze(String mapText) {
		map = mapText.split("\n");
		int numCols = map[0].length(), numRows = map.length;
		for (int row = 0; row < numRows; ++row) {
			for (int col = 0; col < numCols; ++col) {
				char c = map(row, col);
				Tile tile = new Tile(col, row);
				if (c == BLINKY_HOME) {
					blinkyHome = tile;
				} else if (c == PINKY_HOME) {
					pinkyHome = tile;
				} else if (c == INKY_HOME) {
					inkyHome = tile;
				} else if (c == CLYDE_HOME) {
					clydeHome = tile;
				} else if (c == BONUS) {
					bonusTile = tile;
				} else if (c == PACMAN_HOME) {
					pacManHome = tile;
				} else if (c == BLINKY_ST) {
					blinkyScatteringTarget = tile;
				} else if (c == PINKY_ST) {
					pinkyScatteringTarget = tile;
				} else if (c == INKY_ST) {
					inkyScatteringTarget = tile;
				} else if (c == CLYDE_ST) {
					clydeScatteringTarget = tile;
				} else if (c == TUNNEL) {
					tunnelRow = row;
				} else if (c == PELLET || c == ENERGIZER) {
					foodTotal += 1;
				}
			}
		}

		graph = new GridGraph<>(numCols, numRows, NESW, v -> null, (u, v) -> null, UndirectedEdge::new);
		graph.setDefaultVertexLabel(v -> map(graph.row(v), graph.col(v)));
		graph.fill();
		// remove all edges from/to walls
		graph.edges().filter(edge -> graph.get(edge.either()) == WALL || graph.get(edge.other()) == WALL)
				.forEach(graph::removeEdge);
		// identify intersections (free intersections vs. intersections where ghosts cannot move up)
		graph.vertices()
		//@formatter:off
			.filter(cell -> graph.degree(cell) >= 3)
			.filter(cell -> graph.get(cell) != DOOR)
			.filter(cell -> !inGhostHouse(tile(cell)))
			.filter(cell -> !tile(cell).equals(blinkyHome))
			.filter(cell -> !tile(cell).equals(new Tile(blinkyHome.col + 1, blinkyHome.row)))
			//@formatter:on
				.forEach(cell -> {
					Tile tile = tile(cell);
					if (blinkyHome.equals(new Tile(tile.col + 1, tile.row))
							|| blinkyHome.equals(new Tile(tile.col - 2, tile.row))
							|| pacManHome.equals(new Tile(tile.col + 1, tile.row))
							|| pacManHome.equals(new Tile(tile.col - 2, tile.row))) {
						notUpIntersections.add(tile);
					} else {
						freeIntersections.add(tile);
					}
				});
	}

	private char map(int row, int col) {
		return map[row].charAt(col);
	}

	public GridGraph2D<Character, Void> getGraph() {
		return graph;
	}

	public int numCols() {
		return graph.numCols();
	}

	public int numRows() {
		return graph.numRows();
	}

	public Stream<Tile> tiles() {
		return graph.vertices().mapToObj(this::tile);
	}

	public boolean isValidTile(Tile tile) {
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

	public Tile getBlinkyScatteringTarget() {
		return blinkyScatteringTarget;
	}

	public Tile getPinkyScatteringTarget() {
		return pinkyScatteringTarget;
	}

	public Tile getInkyScatteringTarget() {
		return inkyScatteringTarget;
	}

	public Tile getClydeScatteringTarget() {
		return clydeScatteringTarget;
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

	public Tile getLeftTunnelEntry() {
		return new Tile(0, tunnelRow);
	}

	public Tile getRightTunnelEntry() {
		return new Tile(numCols() - 1, tunnelRow);
	}

	private char getContent(Tile tile) {
		if (inTeleportSpace(tile)) {
			return SPACE;
		}
		if (!isValidTile(tile)) {
			throw new IllegalArgumentException("Cannot access maze content for invalid tile: " + tile);
		}
		return graph.get(cell(tile));
	}

	public boolean inTeleportSpace(Tile tile) {
		return tile.row == tunnelRow && (tile.col == -1 || tile.col == numCols());
	}

	public boolean inTunnel(Tile tile) {
		return getContent(tile) == TUNNEL;
	}

	public boolean isWall(Tile tile) {
		return getContent(tile) == WALL;
	}

	public boolean isDoor(Tile tile) {
		return getContent(tile) == DOOR;
	}

	public boolean isGhostHouseEntry(Tile tile) {
		return isValidTile(tile) && isDoor(neighborTile(tile, Top4.S).get());
	}

	public boolean isFreeIntersection(Tile tile) {
		return freeIntersections.contains(tile);
	}

	public boolean isNotUpIntersection(Tile tile) {
		return notUpIntersections.contains(tile);
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
			GraphTraversal pathfinder =
					// new AStarTraversal<>(graph, edge -> 1, graph::manhattan);
					new BreadthFirstTraversal<>(graph);
			pathfinder.traverseGraph(cell(source), cell(target));
			pathFinderCalls += 1;
			if (pathFinderCalls % 100 == 0) {
				Application.LOGGER.info(String.format("%d'th pathfinding executed", pathFinderCalls));
			}
			return pathfinder.path(cell(target)).stream().map(this::tile).collect(Collectors.toList());
		}
		return Collections.emptyList();
	}

	public int euclidean2(Tile t1, Tile t2) {
		return graph.euclidean2(cell(t1), cell(t2));
	}

	public int manhattan(Tile t1, Tile t2) {
		return graph.manhattan(cell(t1), cell(t2));
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