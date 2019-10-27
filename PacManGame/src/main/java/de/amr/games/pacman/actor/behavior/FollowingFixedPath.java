package de.amr.games.pacman.actor.behavior;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import de.amr.games.pacman.actor.MazeMover;
import de.amr.games.pacman.model.Tile;

/**
 * Computes a fixed path and lets the actor follow this path until the target tile is reached.
 *
 * @author Armin Reichert
 *
 * @param <T>
 *          actor type
 */
class FollowingFixedPath<T extends MazeMover> implements Behavior<T> {

	protected Supplier<Tile> targetTileSupplier;
	protected List<Tile> path = Collections.emptyList();

	public FollowingFixedPath(Supplier<Tile> targetTileSupplier) {
		this.targetTileSupplier = targetTileSupplier;
	}

	@Override
	public Route getRoute(T actor) {
		if (path.size() == 0 || actor.tilePosition().equals(path.get(path.size() - 1))) {
			computePath(actor);
		}
		while (path.size() > 0 && !actor.tilePosition().equals(path.get(0))) {
			path.remove(0);
		}
		Route route = new Route();
		route.setPath(path);
		route.setDir(actor.game.maze.alongPath(path).orElse(actor.getMoveDir()));
		return route;
	}

	@Override
	public void computePath(T actor) {
		path = actor.game.maze.findPath(actor.tilePosition(), targetTileSupplier.get());
	}
}