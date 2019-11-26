package de.amr.games.pacman.actor.behavior.pacman;

import static de.amr.datastruct.StreamUtils.permute;
import static de.amr.games.pacman.model.Maze.NESW;
import static de.amr.games.pacman.model.Tile.distance;

import java.util.Comparator;

import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.actor.PacMan;
import de.amr.games.pacman.actor.behavior.Steering;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;

public class AvoidGhosts implements Steering<PacMan> {

	private final Maze maze;

	public AvoidGhosts(Maze maze) {
		this.maze = maze;
	}

	@Override
	public void steer(PacMan pacMan) {
		/*@formatter:off*/
		pacMan.game.activeGhosts()
			.filter(ghost -> !maze.inGhostHouse(ghost.currentTile()))
			.sorted(bySmallestDistanceTo(pacMan))
			.findFirst()
			.ifPresent(ghost -> {
				pacMan.nextDir = NESW.dirs().boxed()
						.filter(pacMan::canCrossBorderTo)
						.sorted(byLargestDistanceOfNeighborTile(pacMan, ghost))
						.findAny()
						.orElse(randomAccessibleDir(pacMan));
			});
		/*@formatter:on*/
	}

	private Comparator<Integer> byLargestDistanceOfNeighborTile(PacMan pacMan, Ghost ghost) {
		Tile pacManTile = pacMan.currentTile(), ghostTile = ghost.currentTile();
		return (dir1, dir2) -> {
			Tile neighborTile1 = maze.tileToDir(pacManTile, dir1), neighborTile2 = maze.tileToDir(pacManTile, dir2);
			return -Integer.compare(distance(neighborTile1, ghostTile), distance(neighborTile2, ghostTile));
		};
	}

	private Comparator<Ghost> bySmallestDistanceTo(PacMan pacMan) {
		return (ghost1, ghost2) -> Integer.compare(pacMan.distanceTo(ghost1), pacMan.distanceTo(ghost2));
	}

	private int randomAccessibleDir(PacMan pacMan) {
		return permute(NESW.dirs()).filter(pacMan::canCrossBorderTo).findAny().getAsInt();
	}
}