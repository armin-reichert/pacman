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
	protected List<Tile> targetPath;

	public TakingPrecomputedPath(Maze maze, Supplier<Tile> fnTargetTile) {
		this.maze = maze;
		this.fnTargetTile = fnTargetTile;
		this.targetPath = new ArrayList<>();
	}

	@Override
	public List<Tile> targetPath() {
		return targetPath;
	}

	protected abstract List<Tile> computePath(T actor, Tile targetTile);

	protected boolean isPathInvalid(T actor) {
		return actor.wishDir() == null || targetPath.size() == 0 || first(targetPath) != actor.tile()
				|| last(targetPath) != actor.targetTile();
	}

	@Override
	public void steer(T actor) {
		Tile targetTile = fnTargetTile.get();
		if (targetTile == null) {
			actor.setTargetTile(null);
			targetPath = Collections.emptyList();
			return;
		}
		for (int i = 0; i < targetPath.indexOf(actor.tile()); ++i) {
			targetPath.remove(0);
		}
		if (isPathInvalid(actor)) {
			targetPath = computePath(actor, targetTile);
			actor.setTargetTile(last(targetPath));
		}
		actor.setWishDir(maze.alongPath(targetPath).orElse(null));
	}
}