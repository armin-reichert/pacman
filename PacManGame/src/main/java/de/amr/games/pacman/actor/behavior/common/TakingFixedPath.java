package de.amr.games.pacman.actor.behavior.common;

import java.util.ArrayList;
import java.util.List;

import de.amr.games.pacman.actor.core.MazeMover;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;

public class TakingFixedPath<T extends MazeMover> extends TakingPrecomputedPath<T> {

	private List<Tile> path;

	public TakingFixedPath(Maze maze, List<Tile> path) {
		super(maze, () -> path.get(path.size() - 1));
		this.path = new ArrayList<>(path);
	}

	@Override
	protected List<Tile> computePath(T actor, Tile targetTile) {
		return path;
	}
}
