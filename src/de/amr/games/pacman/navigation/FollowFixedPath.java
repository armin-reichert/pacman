package de.amr.games.pacman.navigation;

import java.util.Collections;
import java.util.List;

import de.amr.games.pacman.actor.MazeMover;
import de.amr.games.pacman.model.Tile;

//TODO: does not yet work 100%
class FollowFixedPath implements Navigation {

	protected final Tile target;
	protected List<Tile> path;
	protected Tile prevMoverTile;

	public FollowFixedPath(Tile target) {
		this.target = target;
		path = Collections.emptyList();
	}

	@Override
	public MazeRoute computeRoute(MazeMover mover) {
		MazeRoute route = new MazeRoute();
		Tile currentMoverTile = mover.getTile();
		if (!currentMoverTile.equals(prevMoverTile) || mover.isStuck()) {
			prevMoverTile = currentMoverTile;
			if (path.size() >= 1) {
				path.remove(0);
			}
			route.path = path;
			route.dir = mover.getMaze().alongPath(path).orElse(mover.getCurrentDir());
		}
		return route;
	}

	@Override
	public void prepareRoute(MazeMover mover) {
		path = mover.getMaze().findPath(mover.getTile(), target);
		prevMoverTile = mover.getTile();
	}
}