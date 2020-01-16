package de.amr.games.pacman.actor.steering.common;

import java.util.List;
import java.util.function.Supplier;

import de.amr.games.pacman.model.MazeGraph;
import de.amr.games.pacman.model.MazeMoving;
import de.amr.games.pacman.model.Tile;

/**
 * Steering which computes the shortest path (using graph path finding) to the
 * target tile.
 *
 * @author Armin Reichert
 */
public class TakingShortestPath extends TakingPrecomputedPath {

	protected MazeGraph graph;

	public TakingShortestPath(MazeMoving actor, Supplier<Tile> fnTargetTile) {
		super(actor, fnTargetTile);
		graph = new MazeGraph(maze);
	}

	@Override
	protected List<Tile> pathToTarget(MazeMoving actor, Tile targetTile) {
		return graph.shortestPath(actor.tile(), targetTile);
	}
}