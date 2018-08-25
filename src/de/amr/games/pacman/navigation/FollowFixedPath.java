package de.amr.games.pacman.navigation;

import java.util.Collections;
import java.util.List;

import de.amr.games.pacman.actor.MazeMover;
import de.amr.games.pacman.model.Tile;

//TODO: does not yet work 100%
class FollowFixedPath implements Navigation {

	protected Tile target;
	protected List<Tile> path = Collections.emptyList();

	public FollowFixedPath(Tile target) {
		this.target = target;
	}
	
	protected FollowFixedPath() {
		
	}

	@Override
	public MazeRoute computeRoute(MazeMover mover) {
		MazeRoute route = new MazeRoute();
		if (path.size() > 0 && !path.get(0).equals(mover.getTile())) {
			path.remove(0);
		}
		route.path = path;
		route.dir = mover.getMaze().alongPath(path).orElse(mover.getCurrentDir());
		return route;
	}

	@Override
	public void prepareRoute(MazeMover mover) {
		path = mover.getMaze().findPath(mover.getTile(), target);
	}
}