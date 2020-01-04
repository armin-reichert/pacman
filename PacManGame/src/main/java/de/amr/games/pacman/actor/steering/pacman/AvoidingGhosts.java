package de.amr.games.pacman.actor.steering.pacman;

import static de.amr.datastruct.StreamUtils.permute;
import static de.amr.games.pacman.model.Tile.distanceSq;

import java.util.Comparator;

import de.amr.games.pacman.actor.Cast;
import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.actor.PacMan;
import de.amr.games.pacman.actor.core.MazeMover;
import de.amr.games.pacman.actor.steering.core.Steering;
import de.amr.games.pacman.model.Direction;
import de.amr.games.pacman.model.Tile;

public class AvoidingGhosts implements Steering {

	private final Cast cast;

	public AvoidingGhosts(Cast cast) {
		this.cast = cast;
	}

	@Override
	public void init() {
	}

	@Override
	public void force() {
	}

	@Override
	public void enableTargetPathComputation(boolean b) {
	}

	@Override
	public boolean requiresGridAlignment() {
		return true;
	}

	@Override
	public void steer() {
		PacMan pacMan = cast.pacMan;
		/*@formatter:off*/
		cast.ghostsOnStage()
			.filter(ghost -> !pacMan.maze().inGhostHouse(ghost.tile()))
			.sorted(bySmallestDistanceTo(pacMan))
			.findFirst()
			.ifPresent(ghost -> {
				pacMan.setWishDir(Direction.dirs()
						.filter(pacMan::canCrossBorderTo)
						.sorted(byLargestDistanceOfNeighborTile(pacMan, ghost))
						.findAny()
						.orElse(randomAccessibleDir(pacMan)));
			});
		/*@formatter:on*/
	}

	private Comparator<Direction> byLargestDistanceOfNeighborTile(PacMan pacMan, MazeMover ghost) {
		Tile pacManTile = pacMan.tile(), ghostTile = ghost.tile();
		return (dir1, dir2) -> {
			Tile neighborTile1 = pacMan.maze().tileToDir(pacManTile, dir1),
					neighborTile2 = pacMan.maze().tileToDir(pacManTile, dir2);
			return -Integer.compare(distanceSq(neighborTile1, ghostTile), distanceSq(neighborTile2, ghostTile));
		};
	}

	private Comparator<Ghost> bySmallestDistanceTo(PacMan pacMan) {
		return (ghost1, ghost2) -> Integer.compare(distanceSq(pacMan.tile(), ghost1.tile()),
				distanceSq(pacMan.tile(), ghost2.tile()));
	}

	private Direction randomAccessibleDir(PacMan pacMan) {
		return permute(Direction.dirs()).filter(pacMan::canCrossBorderTo).findAny().get();
	}
}