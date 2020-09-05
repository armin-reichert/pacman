package de.amr.games.pacman.controller.steering.common;

import java.util.function.Supplier;

import de.amr.games.pacman.controller.creatures.Guy;
import de.amr.games.pacman.model.world.components.Tile;
import de.amr.games.pacman.model.world.graph.WorldGraph;

/**
 * Steers a guy following the shortest path (using graph path finding) to the target tile.
 *
 * @author Armin Reichert
 */
public class TakingShortestPath extends FollowingPath {

	private final WorldGraph graph;
	private final Supplier<Tile> fnTargetTile;

	public TakingShortestPath(Guy<?> guy, Supplier<Tile> fnTargetTile) {
		super(guy);
		this.fnTargetTile = fnTargetTile;
		graph = new WorldGraph(guy.world);
	}

	@Override
	public void steer(Guy<?> guy) {
		if (path.size() == 0 || isComplete()) {
			setPath(graph.findPath(guy.tile(), fnTargetTile.get()));
		}
		super.steer(guy);
	}
}