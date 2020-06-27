package de.amr.games.pacman.controller.actor.steering.common;

import java.util.List;
import java.util.function.Supplier;

import de.amr.games.pacman.controller.actor.MazeMover;
import de.amr.games.pacman.model.world.Tile;
import de.amr.games.pacman.model.world.WorldGraph;

/**
 * Lets an actor follow the shortest path (using graph path finding) to the
 * target tile.
 *
 * @author Armin Reichert
 */
public class TakingShortestPath extends TakingPrecomputedPath {

	protected WorldGraph graph;

	public TakingShortestPath(MazeMover actor, Supplier<Tile> fnTargetTile) {
		super(actor, fnTargetTile);
		graph = new WorldGraph(maze);
	}

	@Override
	protected List<Tile> pathToTarget(MazeMover actor, Tile targetTile) {
		return graph.shortestPath(actor.tile(), targetTile);
	}
}