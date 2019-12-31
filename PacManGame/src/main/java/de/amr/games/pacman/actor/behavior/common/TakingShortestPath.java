package de.amr.games.pacman.actor.behavior.common;

import java.util.List;
import java.util.function.Supplier;

import de.amr.games.pacman.actor.core.MazeMover;
import de.amr.games.pacman.model.MazeGraph;
import de.amr.games.pacman.model.Tile;

/**
 * Steering which computes the shortest path (using graph path finding) to the
 * target tile.
 *
 * @author Armin Reichert
 */
public class TakingShortestPath<T extends MazeMover> extends TakingPrecomputedPath<T> {

	protected MazeGraph graph;

	public TakingShortestPath(T actor, Supplier<Tile> fnTargetTile) {
		super(actor, fnTargetTile);
		graph = new MazeGraph(maze);
	}

	@Override
	protected List<Tile> computePath(T actor, Tile targetTile) {
		return graph.shortestPath(actor.tile(), targetTile);
	}
}