package de.amr.games.pacman.actor.steering.common;

import java.util.ArrayList;
import java.util.List;

import de.amr.games.pacman.actor.MazeMover;
import de.amr.games.pacman.model.tiles.Tile;

/**
 * Lets an actor follow a fixed path.
 * 
 * @author Armin Reichert
 */
public class TakingFixedPath extends TakingPrecomputedPath {

	private List<Tile> path;

	public TakingFixedPath(MazeMover actor, List<Tile> path) {
		super(actor, () -> path.get(path.size() - 1));
		this.path = new ArrayList<>(path);
	}

	@Override
	protected List<Tile> pathToTarget(MazeMover actor, Tile targetTile) {
		return path;
	}
}