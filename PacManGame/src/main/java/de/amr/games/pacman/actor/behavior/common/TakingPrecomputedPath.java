package de.amr.games.pacman.actor.behavior.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import de.amr.games.pacman.actor.behavior.Steering;
import de.amr.games.pacman.actor.core.MazeMover;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;

/**
 * Steering using a precomputed path.
 *
 * @author Armin Reichert
 */
public abstract class TakingPrecomputedPath<T extends MazeMover> implements Steering<T> {

	static Tile first(List<Tile> list) {
		return list.isEmpty() ? null : list.get(0);
	}

	static Tile last(List<Tile> list) {
		return list.isEmpty() ? null : list.get(list.size() - 1);
	}

	protected final Maze maze;
	protected final Supplier<Tile> fnTargetTile;
	protected List<Tile> path;

	public TakingPrecomputedPath(Maze maze, Supplier<Tile> fnTargetTile) {
		this.maze = maze;
		this.fnTargetTile = fnTargetTile;
		this.path = new ArrayList<>();
	}

	protected abstract List<Tile> computePath(T actor, Tile targetTile);

	protected boolean isPathInvalid(T actor) {
		return actor.wishDir() == null || path.size() == 0 || first(path) != actor.tile()
				|| last(path) != actor.targetTile();
	}

	@Override
	public void steer(T actor) {
		Tile targetTile = fnTargetTile.get();
		if (targetTile == null) {
			actor.setTargetTile(null);
			actor.setTargetPath(Collections.emptyList());
			return;
		}
		for (int i = 0; i < path.indexOf(actor.tile()); ++i) {
			path.remove(0);
		}
		if (isPathInvalid(actor)) {
			path = computePath(actor, targetTile);
			actor.setTargetTile(last(path));
			actor.setTargetPath(path);
		}
		actor.setWishDir(maze.alongPath(path).orElse(null));
	}
}