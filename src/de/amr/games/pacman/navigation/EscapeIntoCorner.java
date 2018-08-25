package de.amr.games.pacman.navigation;

import java.util.Arrays;
import java.util.List;

import de.amr.games.pacman.actor.MazeMover;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;

class EscapeIntoCorner implements Navigation {

	private final MazeMover chaser;
	private Tile corner;

	public static Tile chooseCorner(MazeMover refugee, MazeMover chaser) {
		Maze maze = chaser.getMaze();
		List<Tile> corners = Arrays.asList(maze.getTopLeftCorner(), maze.getTopRightCorner(), maze.getBottomLeftCorner(),
				maze.getBottomRightCorner());

		Tile chaserTile = chaser.getTile();
		boolean left = chaserTile.col <= maze.numCols() / 2, right = !left;
		boolean top = chaserTile.row <= maze.numRows() / 2, bottom = !top;
		Tile forbiddenCorner;
		if (top && left) {
			forbiddenCorner = maze.getTopLeftCorner();
		} else if (bottom && left) {
			forbiddenCorner = maze.getBottomLeftCorner();
		} else if (top && right) {
			forbiddenCorner = maze.getTopRightCorner();
		} else if (bottom && right) {
			forbiddenCorner = maze.getBottomRightCorner();
		} else {
			forbiddenCorner = null;
		}
		return corners.stream().filter(corner -> !corner.equals(forbiddenCorner)).findFirst().get();
	}

	public EscapeIntoCorner(MazeMover chaser) {
		this.chaser = chaser;
	}

	@Override
	public MazeRoute computeRoute(MazeMover refugee) {
		MazeRoute route = new MazeRoute();
		Maze maze = refugee.getMaze();
		corner = chooseCorner(refugee, chaser);
		route.path = maze.findPath(refugee.getTile(), corner);
		route.dir = maze.alongPath(route.path).orElse(refugee.getCurrentDir());
		return route;
	}

	@Override
	public void prepareRoute(MazeMover refugee) {
	}
}