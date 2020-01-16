package de.amr.games.pacman.actor.steering.common;

import java.util.ArrayList;
import java.util.List;

import de.amr.games.pacman.model.MazeMoving;
import de.amr.games.pacman.model.Tile;

public class TakingFixedPath extends TakingPrecomputedPath {

	private List<Tile> path;

	public TakingFixedPath(MazeMoving actor, List<Tile> path) {
		super(actor, () -> path.get(path.size() - 1));
		this.path = new ArrayList<>(path);
	}

	@Override
	protected List<Tile> pathToTarget(MazeMoving actor, Tile targetTile) {
		return path;
	}
}