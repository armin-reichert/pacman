package de.amr.games.pacman.actor.behavior;

import static de.amr.games.pacman.model.Maze.NESW;
import static de.amr.games.pacman.model.Tile.distance;
import static de.amr.graph.grid.impl.Top4.E;
import static de.amr.graph.grid.impl.Top4.N;
import static de.amr.graph.grid.impl.Top4.S;
import static de.amr.graph.grid.impl.Top4.W;

import java.util.Comparator;
import java.util.stream.IntStream;

import de.amr.datastruct.StreamUtils;
import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.actor.PacMan;
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
						.sorted(byLargestDistanceToGhost(pacMan, ghost))
						.filter(pacMan::canEnterTileTo)
						.findAny()
						.orElse(randomAccessibleDir(pacMan));
			});
		/*@formatter:on*/
	}

	private Comparator<Integer> byLargestDistanceToGhost(PacMan pacMan, Ghost ghost) {
		return (dir1, dir2) -> {
			Tile neighborTile1 = maze.tileToDir(pacMan.currentTile(), dir1);
			Tile neighborTile2 = maze.tileToDir(pacMan.currentTile(), dir2);
			return -Integer.compare(distance(neighborTile1, ghost.currentTile()),
					distance(neighborTile2, ghost.currentTile()));
		};
	}

	private Comparator<Ghost> bySmallestDistanceTo(PacMan pacMan) {
		return (ghost1, ghost2) -> {
			int dist1 = distance(pacMan.currentTile(), ghost1.currentTile());
			int dist2 = distance(pacMan.currentTile(), ghost2.currentTile());
			return Integer.compare(dist1, dist2);
		};
	}

	private int randomAccessibleDir(PacMan pacMan) {
		return StreamUtils.permute(IntStream.of(N, E, S, W)).filter(pacMan::canEnterTileTo).findAny().getAsInt();
	}
}