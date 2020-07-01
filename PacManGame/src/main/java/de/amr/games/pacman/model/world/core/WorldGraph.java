package de.amr.games.pacman.model.world.core;

import static de.amr.easy.game.Application.loginfo;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import de.amr.games.pacman.PacManApp;
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
 * Adds a grid graph structure to the world such that graph path finder algorithms can be used.
 * 
 * @author Armin Reichert
 */
public class WorldGraph extends GridGraph<Tile, Void> {

	private final World world;
	private int pathFinderCalls;

	public WorldGraph(World world) {
		super(world.width(), world.height(), Grid4Topology.get(), v -> null, (u, v) -> null, UndirectedEdge::new);
		setDefaultVertexLabel(this::tile);
		this.world = world;
		fill();
		//@formatter:off
		edges()
			.filter(edge -> !world.isAccessible(tile(edge.either())) || !world.isAccessible(tile(edge.other())))
			.forEach(this::removeEdge);
		/*@formatter:on*/
	}

	public int vertex(Tile tile) {
		return cell(tile.col, tile.row);
	}

	public Tile tile(int vertex) {
		return Tile.at(col(vertex), row(vertex));
	}

	public List<Tile> shortestPath(Tile source, Tile target) {
		if (world.contains(source) && world.contains(target)) {
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
			return new AStarSearch(this, (u, v) -> 1, this::manhattan);
		case "bfs":
			return new BreadthFirstSearch(this);
		case "bestfs":
			return new BestFirstSearch(this, v -> manhattan(v, vertex(target)));
		default:
			return new AStarSearch(this, (u, v) -> 1, this::manhattan);
		}
	}
}