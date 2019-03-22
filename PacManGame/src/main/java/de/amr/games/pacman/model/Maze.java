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
import de.amr.easy.game.assets.Assets;
import de.amr.graph.core.api.UndirectedEdge;
import de.amr.graph.grid.api.GridGraph2D;
import de.amr.graph.grid.api.Topology;
import de.amr.graph.grid.impl.GridGraph;
import de.amr.graph.grid.impl.Top4;
import de.amr.graph.pathfinder.api.Path;
import de.amr.graph.pathfinder.impl.AStarSearch;
import de.amr.graph.pathfinder.impl.GraphSearch;

/**
 * The original Pac-Man maze.
 * 
 * <p>
 * It is represented by a (grid) graph which may store content and can be used by path finding
 * algorithms. The original Pac-Man "AI" does not need a graph structure but we use also path
 * finding in a graph for the game.
 * 
 * @author Armin Reichert
 * 
 * @see GridGraph2D
 */
public class Maze {

	/** The four move directions: NORTH, EAST, SOUTH and WEST. */
	public static final Topology NESW = Top4.get();

	private static final char WALL = '#';
	private static final char DOOR = 'D';
	private static final char TUNNEL = 'T';
	private static final char SPACE = ' ';
	private static final char PELLET = '.';
	private static final char ENERGIZER = '*';
	private static final char EATEN = ':';

	private final String[] map;
	private final GridGraph<Character, Void> grid;

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

	private Set<Tile> unrestrictedIS = new HashSet<>();
	private Set<Tile> upwardsBlockedIS = new HashSet<>();

	private long pathFinderCalls;

	public Maze() {
		map = Assets.text("maze.txt").split("\n");
		int numCols = map[0].length(), numRows = map.length;
		for (int row = 0; row < numRows; ++row) {
			for (int col = 0; col < numCols; ++col) {
				Tile tile = new Tile(col, row);
				switch (map(row, col)) {
				case 'O':
					pacManHome = tile;
					break;
				case 'B':
					blinkyHome = tile;
					break;
				case 'P':
					pinkyHome = tile;
					break;
				case 'I':
					inkyHome = tile;
					break;
				case 'C':
					clydeHome = tile;
					break;
				case 'b':
					blinkyScatteringTarget = tile;
					break;
				case 'p':
					pinkyScatteringTarget = tile;
					break;
				case 'i':
					inkyScatteringTarget = tile;
					break;
				case 'c':
					clydeScatteringTarget = tile;
					break;
				case '$':
					bonusTile = tile;
					break;
				case TUNNEL:
					tunnelRow = row;
					break;
				case PELLET:
				case ENERGIZER:
					foodTotal += 1;
					break;
				default:
					break;
				}
			}
		}

		// The graph represents the maze and stores the maze content inside its vertices.
		grid = new GridGraph<>(numCols, numRows, NESW, v -> null, (u, v) -> null, UndirectedEdge::new);
		grid.setDefaultVertexLabel(v -> map(grid.row(v), grid.col(v)));

		// Add graph edges
		grid.fill();
		// remove edges into walls
		grid.edges().filter(edge -> grid.get(edge.either()) == WALL || grid.get(edge.other()) == WALL)
				.forEach(grid::removeEdge);

		// find intersections (unrestricted ones vs. intersections where ghosts cannot move upwards)
		grid.vertices()
		//@formatter:off
				.filter(cell -> grid.degree(cell) >= 3)
				.filter(cell -> grid.get(cell) != DOOR)
				.filter(cell -> !inGhostHouse(tile(cell)))
				.filter(cell -> !tile(cell).equals(blinkyHome))
				.filter(cell -> !tile(cell).equals(blinkyHome.tileTowards(Top4.E)))
		//@formatter:on
				.forEach(cell -> {
					Tile tile = tile(cell);
					if (blinkyHome.equals(new Tile(tile.col + 1, tile.row))
							|| blinkyHome.equals(new Tile(tile.col - 2, tile.row))
							|| pacManHome.equals(new Tile(tile.col + 1, tile.row))
							|| pacManHome.equals(new Tile(tile.col - 2, tile.row))) {
						upwardsBlockedIS.add(tile);
					}
					else {
						unrestrictedIS.add(tile);
					}
				});
	}

	private char map(int row, int col) {
		return map[row].charAt(col);
	}

	public GridGraph2D<Character, Void> getGraph() {
		return grid;
	}

	public int numCols() {
		return grid.numCols();
	}

	public int numRows() {
		return grid.numRows();
	}

	public Stream<Tile> tiles() {
		return grid.vertices().mapToObj(this::tile);
	}

	public boolean isValidTile(Tile tile) {
		return grid.isValidCol(tile.col) && grid.isValidRow(tile.row);
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
		return inTeleportSpace(tile) ? SPACE : grid.get(cell(tile));
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

	public boolean isUnrestrictedIntersection(Tile tile) {
		return unrestrictedIS.contains(tile);
	}

	public boolean isUpwardsBlockedIntersection(Tile tile) {
		return upwardsBlockedIS.contains(tile);
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
		grid.clearVertexLabels();
	}

	public int getFoodTotal() {
		return foodTotal;
	}

	public void hideFood(Tile tile) {
		grid.set(cell(tile), EATEN);
	}

	public OptionalInt direction(Tile t1, Tile t2) {
		return grid.direction(cell(t1), cell(t2));
	}

	public Optional<Tile> neighborTile(Tile tile, int dir) {
		OptionalInt neighbor = grid.neighbor(cell(tile), dir);
		return neighbor.isPresent() ? Optional.of(tile(neighbor.getAsInt())) : Optional.empty();
	}

	public Stream<Tile> getAdjacentTiles(Tile tile) {
		return grid.adj(cell(tile)).mapToObj(this::tile);
	}

	public List<Tile> findPath(Tile source, Tile target) {
		if (isValidTile(source) && isValidTile(target)) {
			GraphSearch pathfinder = new AStarSearch(grid, (u, v) -> 1, grid::manhattan);
			Path path = Path.computePath(cell(source), cell(target), pathfinder);
			pathFinderCalls += 1;
			if (pathFinderCalls % 100 == 0) {
				Application.LOGGER.info(String.format("%d'th pathfinding executed", pathFinderCalls));
			}
			return path.vertexStream().boxed().map(this::tile).collect(Collectors.toList());
		}
		return Collections.emptyList();
	}

	public double euclidean(Tile t1, Tile t2) {
		return grid.euclidean(cell(t1), cell(t2));
	}

	public double manhattan(Tile t1, Tile t2) {
		return grid.manhattan(cell(t1), cell(t2));
	}

	public OptionalInt alongPath(List<Tile> path) {
		return path.size() < 2 ? OptionalInt.empty() : direction(path.get(0), path.get(1));
	}

	public int cell(Tile tile) {
		return grid.cell(tile.col, tile.row);
	}

	public Tile tile(int cell) {
		return new Tile(grid.col(cell), grid.row(cell));
	}
}