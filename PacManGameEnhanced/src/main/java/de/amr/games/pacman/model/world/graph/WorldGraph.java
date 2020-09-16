package de.amr.games.pacman.model.world.graph;

import static de.amr.easy.game.Application.loginfo;
import static de.amr.games.pacman.PacManApp.settings;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import de.amr.games.pacman.model.world.api.Tile;
import de.amr.games.pacman.model.world.api.World;
import de.amr.graph.core.api.UndirectedEdge;
import de.amr.graph.grid.impl.Grid4Topology;
import de.amr.graph.grid.impl.GridGraph;
import de.amr.graph.pathfinder.api.GraphSearch;
import de.amr.graph.pathfinder.api.Path;
import de.amr.graph.pathfinder.impl.AStarSearch;
import de.amr.graph.pathfinder.impl.BestFirstSearch;
import de.amr.graph.pathfinder.impl.BreadthFirstSearch;

/**
 * Adds a graph structure to the world such that path finder algorithms can be used.
 * 
 * @author Armin Reichert
 */
public class WorldGraph extends GridGraph<Tile, Void> {

	public enum PathFinder {
		ASTAR, BEST_FIRST_SEARCH, BREADTH_FIRST_SEARCH
	}

	public final World world;
	private PathFinder pathFinder;
	private int pathFinderCalls;

	public WorldGraph(World world) {
		super(world.width(), world.height(), Grid4Topology.get(), v -> null, (u, v) -> null, UndirectedEdge::new);
		this.world = world;
		fill();
		edges().filter(edge -> !world.isAccessible(tile(edge.either())) || !world.isAccessible(tile(edge.other())))
				.forEach(this::removeEdge);
		setDefaultVertexLabel(this::tile);
		pathFinder = getPathFinder(settings.pathFinder);
	}

	public void setPathFinder(PathFinder pathFinder) {
		this.pathFinder = pathFinder;
	}

	private PathFinder getPathFinder(String spec) {
		switch (spec.toLowerCase()) {
		case "bfs":
			return PathFinder.BREADTH_FIRST_SEARCH;
		case "bestfs":
			return PathFinder.BEST_FIRST_SEARCH;
		case "astar":
		default:
			return PathFinder.ASTAR;
		}
	}

	private GraphSearch createPathFinder(Tile target) {
		switch (pathFinder) {
		case BREADTH_FIRST_SEARCH:
			return new BreadthFirstSearch(this);
		case BEST_FIRST_SEARCH:
			return new BestFirstSearch(this, v -> manhattan(v, vertex(target)));
		case ASTAR:
		default:
			return new AStarSearch(this, (u, v) -> 1, this::manhattan);
		}
	}

	public int vertex(Tile tile) {
		return cell(tile.col, tile.row);
	}

	public Tile tile(int vertex) {
		return Tile.at(col(vertex), row(vertex));
	}

	public List<Tile> findPath(Tile source, Tile target) {
		List<Tile> tiles = Collections.emptyList();
		if (world.includes(source) && world.includes(target)) {
			Path path = createPathFinder(target).findPath(vertex(source), vertex(target));
			pathFinderCalls += 1;
			if (pathFinderCalls % 100 == 0) {
				loginfo("%d'th pathfinding (%s) executed", pathFinderCalls, pathFinder);
			}
			tiles = path.vertexStream().map(this::tile).collect(Collectors.toList());
		}
		return tiles;
	}
}