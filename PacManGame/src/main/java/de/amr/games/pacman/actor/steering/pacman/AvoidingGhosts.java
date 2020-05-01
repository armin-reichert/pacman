package de.amr.games.pacman.actor.steering.pacman;

import static de.amr.datastruct.StreamUtils.permute;

import java.util.Comparator;

import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.actor.MazeMover;
import de.amr.games.pacman.actor.PacMan;
import de.amr.games.pacman.actor.steering.Steering;
import de.amr.games.pacman.model.Direction;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.tiles.Tile;

public class AvoidingGhosts implements Steering {

	private final Game game;

	public AvoidingGhosts(Game game) {
		this.game = game;
	}

	@Override
	public boolean requiresGridAlignment() {
		return true;
	}

	@Override
	public void steer() {
		PacMan pacMan = game.pacMan;
		/*@formatter:off*/
		game.ghostsOnStage()
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
			return -Integer.compare(neighborTile1.distSq(ghostTile), neighborTile2.distSq(ghostTile));
		};
	}

	private Comparator<Ghost> bySmallestDistanceTo(PacMan pacMan) {
		return (ghost1, ghost2) -> Integer.compare(pacMan.tile().distSq(ghost1.tile()),
				pacMan.tile().distSq(ghost2.tile()));
	}

	private Direction randomAccessibleDir(PacMan pacMan) {
		return permute(Direction.dirs()).filter(pacMan::canCrossBorderTo).findAny().get();
	}
}