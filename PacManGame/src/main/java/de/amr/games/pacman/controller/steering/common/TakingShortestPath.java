package de.amr.games.pacman.controller.steering.common;

import java.util.List;
import java.util.function.Supplier;

import de.amr.games.pacman.controller.api.MobileCreature;
import de.amr.games.pacman.model.world.core.Tile;
import de.amr.games.pacman.model.world.core.WorldGraph;

/**
 * Lets an actor follow the shortest path (using graph path finding) to the target tile.
 *
 * @author Armin Reichert
 */
public class TakingShortestPath extends TakingPrecomputedPath {

	protected WorldGraph graph;

	public TakingShortestPath(MobileCreature actor, Supplier<Tile> fnTargetTile) {
		super(actor, fnTargetTile);
		graph = new WorldGraph(world);
	}

	@Override
	protected List<Tile> pathToTarget(MobileCreature actor, Tile targetTile) {
		return graph.shortestPath(actor.location(), targetTile);
	}
}