package de.amr.games.pacman.actor.behavior;

import static de.amr.graph.grid.impl.Top4.E;
import static de.amr.graph.grid.impl.Top4.N;
import static de.amr.graph.grid.impl.Top4.S;
import static de.amr.graph.grid.impl.Top4.W;

import java.util.Comparator;
import java.util.stream.Stream;

import de.amr.datastruct.StreamUtils;
import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.actor.PacMan;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;

public class AvoidGhosts implements Steering<PacMan> {

	@Override
	public void steer(PacMan pacMan) {
		Maze maze = pacMan.maze;
		if (pacMan.enteredNewTile) {
			/*@formatter:off*/
			pacMan.nextDir = Stream.of(N, E, S, W)
					.filter(pacMan::canEnterTileTo)
					.filter(dir -> dir != Maze.NESW.inv(pacMan.moveDir))
					.sorted(byMinGhostDist(maze, pacMan).reversed())
					.findFirst()
					.orElse(randomDir(pacMan));
			/*@formatter:on*/
		}
	}

	private int minGhostDist(Maze maze, Tile tile, Stream<Ghost> ghosts) {
		/*@formatter:off*/
		return ghosts
//				.map(ghost -> maze.findPath(tile, ghost.currentTile()).size())
				.map(ghost -> Tile.distance(tile, ghost.currentTile()))
				.min(Integer::compare)
				.get();
		/*@formatter:on*/
	}

	private Comparator<Integer> byMinGhostDist(Maze maze, PacMan pacMan) {
		return (dir1, dir2) -> {
			Tile pacManTile = pacMan.currentTile();
			Tile neighbor1 = maze.tileToDir(pacManTile, dir1);
			Tile neighbor2 = maze.tileToDir(pacManTile, dir2);
			int dist1 = minGhostDist(maze, neighbor1, pacMan.game.activeGhosts()),
					dist2 = minGhostDist(maze, neighbor2, pacMan.game.activeGhosts());
			return Integer.compare(dist1, dist2);
		};
	}

	private int randomDir(PacMan pacMan) {
		return StreamUtils.permute(Stream.of(N, E, S, W)).filter(pacMan::canEnterTileTo).findAny().get();
	}
}
