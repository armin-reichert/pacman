package de.amr.games.pacman.model;

import static de.amr.easy.game.Application.loginfo;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import de.amr.games.pacman.PacManApp;
import de.amr.graph.core.api.UndirectedEdge;
import de.amr.graph.grid.api.GridGraph2D;
import de.amr.graph.grid.impl.Grid4Topology;
import de.amr.graph.grid.impl.GridGraph;
import de.amr.graph.pathfinder.api.GraphSearch;
import de.amr.graph.pathfinder.api.Path;
import de.amr.graph.pathfinder.impl.AStarSearch;
import de.amr.graph.pathfinder.impl.BestFirstSearch;
import de.amr.graph.pathfinder.impl.BreadthFirstSearch;

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
			.filter(edge -> maze.isWall(tile(edge.either())) || maze.isWall(tile(edge.other())))
			.forEach(grid::removeEdge);
		/*@formatter:on*/
	}

	public int vertex(Tile tile) {
		return grid.cell(tile.col, tile.row);
	}

	public Tile tile(int vertex) {
		return Tile.at(grid.col(vertex), grid.row(vertex));
	}

	public List<Tile> shortestPath(Tile source, Tile target) {
		if (maze.insideMap(source) && maze.insideMap(target)) {
			GraphSearch pathfinder = createPathFinder(target);
			Path path = pathfinder.findPath(vertex(source), vertex(target));
			pathFinderCalls += 1;
			loginfo("%d'th pathfinding (%s) executed", pathFinderCalls, pathfinder.getClass().getSimpleName());
			return path.vertexStream().map(this::tile).collect(Collectors.toList());
		}
		return Collections.emptyList();
	}

	private GraphSearch createPathFinder(Tile target) {
		switch (PacManApp.settings.pathFinder) {
		case "astar":
			return new AStarSearch(grid, (u, v) -> 1, grid::manhattan);
		case "bfs":
			return new BreadthFirstSearch(grid);
		case "bestfs":
			return new BestFirstSearch(grid, v -> grid.manhattan(v, vertex(target)));
		default:
			return new AStarSearch(grid, (u, v) -> 1, grid::manhattan);
		}
	}
}