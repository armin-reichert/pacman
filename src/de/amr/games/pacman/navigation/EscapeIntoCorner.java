package de.amr.games.pacman.navigation;

import java.util.Random;
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
		boolean inUpperMazeHalf = chaserTile.row < maze.numRows() / 2;
		Random rnd = new Random();
		if (inUpperMazeHalf) {
			return rnd.nextBoolean() ? maze.getBottomLeftCorner() : maze.getBottomRightCorner();
		} else {
			return rnd.nextBoolean() ? maze.getTopLeftCorner() : maze.getTopRightCorner();
		}
	}
}