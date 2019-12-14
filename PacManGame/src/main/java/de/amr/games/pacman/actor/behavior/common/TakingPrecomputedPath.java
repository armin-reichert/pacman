package de.amr.games.pacman.actor.behavior.common;

import java.util.Collections;
import java.util.List;
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

	public TakingPrecomputedPath(Maze maze, Supplier<Tile> fnTargetTile) {
		this.maze = maze;
		this.fnTargetTile = fnTargetTile;
	}

	@Override
	public void steer(T actor) {
		Tile actorTile = actor.tile();
		actor.setTargetTile(fnTargetTile.get());
		while (pathSuffix.size() > 0 && !actorTile.equals(pathSuffix.get(0))) {
			pathSuffix.remove(0);
		}
		if (pathSuffix.isEmpty() || actorTile.equals(pathSuffix.get(pathSuffix.size() - 1))) {
			pathSuffix = computePath(actor);
			actor.setTargetPath(pathSuffix);
			actor.setTargetTile(pathSuffix.isEmpty() ? null : pathSuffix.get(pathSuffix.size() - 1));
		}
		actor.setNextDir(maze.alongPath(pathSuffix).orElse(null));
	}

	protected abstract List<Tile> computePath(T actor);
}