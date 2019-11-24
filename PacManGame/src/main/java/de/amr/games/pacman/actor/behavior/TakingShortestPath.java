package de.amr.games.pacman.actor.behavior;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import de.amr.games.pacman.actor.MazeMover;
import de.amr.games.pacman.model.Tile;

/**
 * Steering using a graph-based path finder.
 *
 * @author Armin Reichert
 */
class TakingShortestPath implements Steering {

	protected Supplier<Tile> fnTargetTile;
	protected List<Tile> path = Collections.emptyList();

	public TakingShortestPath(Supplier<Tile> fnTargetTile) {
		this.fnTargetTile = fnTargetTile;
	}

	@Override
	public void steer(MazeMover actor) {
		trimPath(actor);
		if (path.isEmpty() || actor.currentTile().equals(path.get(path.size() - 1))) {
			computePath(actor);
			actor.targetPath = path;
		}
		actor.nextDir = actor.maze.alongPath(path).orElse(actor.moveDir);
	}

	protected void computePath(MazeMover actor) {
		path = actor.maze.findPath(actor.currentTile(), fnTargetTile.get());
		trimPath(actor);
	}

	protected void trimPath(MazeMover actor) {
		Tile actorTile = actor.currentTile();
		while (path.size() > 0 && !actorTile.equals(path.get(0))) {
			path.remove(0);
		}
	}
}