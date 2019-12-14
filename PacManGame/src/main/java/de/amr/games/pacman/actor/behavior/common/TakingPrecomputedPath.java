package de.amr.games.pacman.actor.behavior.common;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import de.amr.games.pacman.actor.behavior.Steering;
import de.amr.games.pacman.actor.core.MazeMover;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;

/**
 * Steering using a precomputed path which is recomputed only if the actor would leave this path.
 *
 * @author Armin Reichert
 */
public abstract class TakingPrecomputedPath<T extends MazeMover> implements Steering<T> {

	protected Maze maze;
	protected Supplier<Tile> fnTargetTile;
	protected List<Tile> pathSuffix = Collections.emptyList();
	public Function<MazeMover, Boolean> fnTargetReached;

	public TakingPrecomputedPath(Maze maze, Supplier<Tile> fnTargetTile) {
		this.maze = maze;
		this.fnTargetTile = fnTargetTile;
		this.fnTargetReached = actor -> actor.tile().equals(fnTargetTile.get());
	}

	@Override
	public void steer(T actor) {
		Tile actorTile = actor.tile();
		Tile targetTile = fnTargetTile.get();
		actor.setTargetTile(targetTile);
		if (actor.targetTile() == null) {
			actor.setTargetPath(Collections.emptyList());
			return;
		}
		// is path suffix still useful?
		int pathLength = pathSuffix.size();
		int index = pathSuffix.indexOf(actorTile);
		boolean usable = pathLength >= 2 && index != -1 && last(pathSuffix) == targetTile;
		if (usable) {
			for (int i = 0; i < index; ++i) {
				pathSuffix.remove(0);
			}
			usable = pathSuffix.size() >= 2;
		}
		if (!usable) {
			pathSuffix = computePath(actor);
			actor.setTargetPath(pathSuffix);
		}
		actor.setNextDir(maze.alongPath(pathSuffix).orElse(null));
	}

	static Tile first(List<Tile> list) {
		return list.isEmpty() ? null : list.get(0);
	}

	static Tile last(List<Tile> list) {
		return list.isEmpty() ? null : list.get(list.size() - 1);
	}

	protected abstract List<Tile> computePath(T actor);
}