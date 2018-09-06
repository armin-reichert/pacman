package de.amr.games.pacman.navigation;

import java.util.function.Supplier;

import de.amr.games.pacman.actor.Actor;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;

class EscapeIntoCorner<T extends Actor> extends FollowFixedPath<T> {

	private Tile target;

	public EscapeIntoCorner(Supplier<Tile> chaserTileSupplier) {
		super(chaserTileSupplier);
	}

	@Override
	public MazeRoute computeRoute(T refugee) {
		while (target == null || target.equals(refugee.getTile())) {
			target = chooseCorner(refugee.getMaze());
		}
		return super.computeRoute(refugee);
	}

	@Override
	public void computeStaticRoute(T refugee) {
		Tile target = chooseCorner(refugee.getMaze());
		while (target.equals(refugee.getTile())) {
			target = chooseCorner(refugee.getMaze());
		}
		path = refugee.getMaze().findPath(refugee.getTile(), target);
	}

	private Tile chooseCorner(Maze maze) {
		Tile chaserTile = targetTileSupplier.get();
		boolean chaserTop = chaserTile.row < maze.numRows() / 2;
		boolean chaserLeft = chaserTile.col < maze.numRows() / 2;
		if (chaserTop) {
			return chaserLeft ? maze.getBottomRightCorner() : maze.getBottomLeftCorner();
		} else {
			return chaserLeft ? maze.getTopRightCorner() : maze.getTopLeftCorner();
		}
	}
}