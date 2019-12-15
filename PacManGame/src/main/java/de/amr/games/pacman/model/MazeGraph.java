package de.amr.games.pacman.model;

import static de.amr.easy.game.Application.LOGGER;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import de.amr.graph.core.api.UndirectedEdge;
import de.amr.graph.grid.api.GridGraph2D;
import de.amr.graph.grid.impl.Grid4Topology;
import de.amr.graph.grid.impl.GridGraph;
import de.amr.graph.pathfinder.api.GraphSearch;
import de.amr.graph.pathfinder.api.Path;
import de.amr.graph.pathfinder.impl.AStarSearch;

/**
 * Adds a grid graph structure to the maze such that graph path finder
 * algorithms can be run on the maze.
 * 
 * @author Armin Reichert
 */
public class MazeGraph {

	public final Maze maze;
	public final GridGraph2D<Tile, Void> grid;
	private int pathFinderCalls;

	public MazeGraph(Maze maze) {
		this.maze = maze;
		grid = new GridGraph<>(maze.numCols, maze.numRows, Grid4Topology.get(), this::tile, (u, v) -> null,
				UndirectedEdge::new);
		grid.fill();
		//@formatter:off
		grid.edges()
			.filter(edge -> tile(edge.either()).isWall() || tile(edge.other()).isWall())
			.forEach(grid::removeEdge);
		/*@formatter:on*/
	}

	public int vertex(Tile tile) {
		return grid.cell(tile.col, tile.row);
	}

	public Tile tile(int vertex) {
		return maze.tiles[grid.col(vertex)][grid.row(vertex)];
	}

	public List<Tile> shortestPath(Tile source, Tile target) {
		if (maze.insideBoard(source) && maze.insideBoard(target)) {
			GraphSearch pathfinder = new AStarSearch(grid, (u, v) -> 1, grid::manhattan);
			Path path = pathfinder.findPath(vertex(source), vertex(target));
			pathFinderCalls += 1;
			LOGGER.info(String.format("%d'th pathfinding executed", pathFinderCalls));
			return path.vertexStream().map(this::tile).collect(Collectors.toList());
		}
		return Collections.emptyList();
	}
}