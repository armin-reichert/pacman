package de.amr.games.pacman.navigation;

import java.util.stream.Stream;

import de.amr.easy.util.StreamUtils;
import de.amr.games.pacman.actor.MazeMover;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;

class EscapeIntoCorner<T extends MazeMover> extends FollowFixedPath<T> {

	private final MazeMover chaser;

	public EscapeIntoCorner(MazeMover chaser) {
		this.chaser = chaser;
	}

	@Override
	public void computeStaticRoute(T refugee) {
		target = chooseCorner(refugee.getMaze(), chaser.getTile());
		path = refugee.getMaze().findPath(refugee.getTile(), target);
	}

	private Tile chooseCorner(Maze maze, Tile chaserTile) {
		boolean top = chaserTile.row <= maze.numRows() / 2;
		if (top) {
			return StreamUtils.permute(Stream.of(maze.getBottomLeftCorner(), maze.getBottomRightCorner())).findAny().get();
		} else {
			return StreamUtils.permute(Stream.of(maze.getTopLeftCorner(), maze.getTopRightCorner())).findAny().get();
		}
	}
}