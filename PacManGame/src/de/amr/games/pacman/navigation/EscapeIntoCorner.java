package de.amr.games.pacman.navigation;

import static de.amr.easy.util.StreamUtils.permute;

import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import de.amr.games.pacman.actor.PacManGameActor;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;

/**
 * Lets an actor (ghost) escape to the "best" maze corner depending on Pac-Man's current position.
 * The best corner is defined by the maximum distance of Pac-Man to any tile on the path from the
 * actor's current position to the corner. When the target corner is reached the next corner is
 * computed.
 * 
 * @author Armin Reichert
 *
 * @param <T>
 *          actor type
 */
class EscapeIntoCorner<T extends PacManGameActor> extends FollowFixedPath<T> {

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
		return permute(Stream.of(
			maze.getTopLeftCorner(), maze.getTopRightCorner(),
			maze.getBottomRightCorner(), maze.getBottomLeftCorner()
		)).filter(corner -> !corner.equals(refugeeTile))
			.sorted(byDist(maze, refugeeTile, chaserTile).reversed())
			.findFirst().get();
		//@formatter:on
	}

	private Comparator<Tile> byDist(Maze maze, Tile refugeeTile, Tile chaserTile) {
		return (corner1, corner2) -> {
			int dist1 = distance(maze, maze.findPath(refugeeTile, corner1), chaserTile);
			int dist2 = distance(maze, maze.findPath(refugeeTile, corner2), chaserTile);
			return dist1 - dist2;
		};
	}

	private int distance(Maze maze, List<Tile> path, Tile tile) {
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