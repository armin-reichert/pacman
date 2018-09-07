package de.amr.games.pacman.navigation;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import de.amr.easy.util.StreamUtils;
import de.amr.games.pacman.actor.Actor;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;

class EscapeIntoCorner<T extends Actor> extends FollowFixedPath<T> {

	public EscapeIntoCorner(Supplier<Tile> chaserTileSupplier) {
		super(chaserTileSupplier);
	}

	@Override
	public void computePath(T refugee) {
		Maze maze = refugee.getMaze();
		Tile target = refugee.getTile();
		while (target.equals(refugee.getTile())) {
			target = safeCorner(refugee);
		}
		path = maze.findPath(refugee.getTile(), target);
	}

	private Tile safeCorner(T refugee) {
		Maze maze = refugee.getMaze();
		Tile refugeeTile = refugee.getTile();
		Tile chaserTile = targetTileSupplier.get();
		//@formatter:off
		return mazeCorners(maze)
				.filter(corner -> !corner.equals(refugeeTile))
				.sorted((c1, c2) -> evalCorners(maze, c1, c2, refugeeTile, chaserTile))
				.findFirst().get();
		//@formatter:on
	}

	private Stream<Tile> mazeCorners(Maze maze) {
		return StreamUtils.permute(Stream.of(maze.getTopLeftCorner(), maze.getTopRightCorner(),
				maze.getBottomRightCorner(), maze.getBottomLeftCorner()));
	}

	private int evalCorners(Maze maze, Tile corner1, Tile corner2, Tile refugeeTile, Tile chaserTile) {
		int dist1 = minDist(maze, maze.findPath(refugeeTile, corner1), chaserTile);
		int dist2 = minDist(maze, maze.findPath(refugeeTile, corner2), chaserTile);
		return dist2 - dist1;
	}

	private int minDist(Maze maze, List<Tile> path, Tile tile) {
		int min = Integer.MAX_VALUE;
		for (Tile t : path) {
			int dist = maze.manhattan(t, tile);
			if (dist < min) {
				min = dist;
			}
		}
		return min;
	}
}