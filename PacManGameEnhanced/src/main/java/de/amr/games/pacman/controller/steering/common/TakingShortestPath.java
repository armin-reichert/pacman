package de.amr.games.pacman.controller.steering.common;

import java.util.function.Supplier;

import de.amr.games.pacman.model.world.api.Tile;
import de.amr.games.pacman.model.world.core.MobileLifeform;
import de.amr.games.pacman.model.world.graph.WorldGraph;

/**
 * Lets a lifeform follow the shortest path (using graph path finding) to the target tile.
 *
 * @author Armin Reichert
 */
public class TakingShortestPath extends FollowingPath {

	private final WorldGraph graph;
	private final Supplier<Tile> fnTargetTile;

	public TakingShortestPath(MobileLifeform mover, Supplier<Tile> fnTargetTile) {
		super(mover);
		this.fnTargetTile = fnTargetTile;
		graph = new WorldGraph(mover.world);
	}

	@Override
	public void steer(MobileLifeform mover) {
		if (path.size() == 0 || isComplete()) {
			setPath(graph.shortestPath(mover.tile(), fnTargetTile.get()));
		}
		super.steer(mover);
	}
}