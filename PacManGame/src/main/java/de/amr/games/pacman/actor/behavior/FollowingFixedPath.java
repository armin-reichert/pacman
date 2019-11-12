package de.amr.games.pacman.actor.behavior;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import de.amr.easy.game.Application;
import de.amr.games.pacman.actor.MazeMover;
import de.amr.games.pacman.model.Tile;

/**
 * Computes a fixed path and lets the actor follow this path until the target tile is reached.
 *
 * @author Armin Reichert
 */
class FollowingFixedPath implements SteeringBehavior {

	protected Supplier<Tile> fnTargetTile;
	protected List<Tile> cachedPath = Collections.emptyList();

	public FollowingFixedPath(Supplier<Tile> fnTargetTile) {
		this.fnTargetTile = fnTargetTile;
	}

	@Override
	public void steer(MazeMover actor) {
		trimCachedPath(actor);
		if (cachedPath.isEmpty() || actor.currentTile().equals(cachedPath.get(cachedPath.size() - 1))) {
			computePath(actor);
			actor.targetPath = cachedPath;
		}
		actor.nextDir = actor.maze.alongPath(cachedPath).orElse(actor.moveDir);
	}

	@Override
	public void computePath(MazeMover actor) {
		Application.LOGGER.info("Computing new path");
		cachedPath = actor.maze.findPath(actor.currentTile(), fnTargetTile.get());
		trimCachedPath(actor);
	}

	private void trimCachedPath(MazeMover actor) {
		Tile actorTile = actor.currentTile();
		while (cachedPath.size() > 0 && !actorTile.equals(cachedPath.get(0))) {
			cachedPath.remove(0);
		}
	}
}