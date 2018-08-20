package de.amr.games.pacman.test.navigation;

import java.util.Optional;

import de.amr.easy.game.Application;
import de.amr.games.pacman.actor.core.MazeMover;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.navigation.MazeRoute;
import de.amr.games.pacman.navigation.Navigation;

public class FollowTargetTile implements Navigation {

	private Maze maze;
	private Tile targetTile;

	public FollowTargetTile(Maze maze, Tile targetTile) {
		this.maze = maze;
		this.targetTile = targetTile;
	}

	@Override
	public MazeRoute computeRoute(MazeMover mover) {
		MazeRoute route = new MazeRoute();

		Tile currentTile = mover.getTile();
		int currentDir = mover.getCurrentDir();

		route.targetTile = targetTile;
		route.dir = currentDir;

		int left = Maze.NESW.left(currentDir);
		Optional<Tile> leftTurn = maze.neighborTile(currentTile, left);
		int leftDist = leftTurn.isPresent() && !maze.isWall(leftTurn.get()) ? maze.euclidean2(leftTurn.get(), targetTile)
				: Integer.MAX_VALUE;

		int right = Maze.NESW.right(currentDir);
		Optional<Tile> rightTurn = maze.neighborTile(currentTile, right);
		int rightDist = rightTurn.isPresent() && !maze.isWall(rightTurn.get())
				? maze.euclidean2(rightTurn.get(), targetTile)
				: Integer.MAX_VALUE;

		if (leftDist < rightDist) {
			route.dir = left;
			Application.LOGGER.info(String.format("ldist: %d, rdist: %d, dir: %s, newDir: %s", leftDist, rightDist,
					dir(currentDir), dir(route.dir)));
		} else if (rightDist < leftDist) {
			route.dir = right;
			Application.LOGGER.info(String.format("ldist: %d, rdist: %d, dir: %s, newDir: %s", leftDist, rightDist,
					dir(currentDir), dir(route.dir)));
		}

		return route;
	}

	private String dir(int dir) {
		return String.valueOf("NESW".charAt(dir));
	}
}
