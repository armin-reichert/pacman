package de.amr.games.pacman.actor.behavior.common;

import java.util.List;
import java.util.function.Supplier;

import de.amr.games.pacman.actor.core.MazeMover;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;

/**
 * Steering which computes the shortest path to the target tile.
 *
 * @author Armin Reichert
 */
public class TakingShortestPath<T extends MazeMover> extends TakingPrecomputedPath<T> {

	public TakingShortestPath(Maze maze, Supplier<Tile> fnTargetTile) {
		super(maze, fnTargetTile);
	}

	@Override
	protected List<Tile> computePath(T actor) {
		return graph.findPath(actor.tile(), actor.targetTile());
	}
}