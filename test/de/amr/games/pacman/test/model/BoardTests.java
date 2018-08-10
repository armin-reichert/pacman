package de.amr.games.pacman.test.model;

import static de.amr.games.pacman.model.Content.DOOR;
import static de.amr.games.pacman.model.Content.ENERGIZER;
import static de.amr.games.pacman.model.Content.PELLET;
import static de.amr.games.pacman.model.Content.WALL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.stream.IntStream;

import org.junit.Test;

import de.amr.easy.game.assets.Assets;
import de.amr.games.pacman.model.Maze;

public class BoardTests {

	@Test
	public void testBoardLoading() {
		Maze maze = new Maze(Assets.text("maze.txt"));
		printMaze(maze);

		assertEquals(28, maze.numCols());
		assertEquals(31, maze.numRows());

		assertEquals(4, maze.tiles().filter(tile -> maze.getContent(tile) == ENERGIZER).count());
		assertEquals(240, maze.tiles().filter(tile -> maze.getContent(tile) == PELLET).count());

		assertTrue(WALL == maze.getContent(0, 3));
		assertTrue(PELLET == maze.getContent(1, 4));
		assertTrue(ENERGIZER == maze.getContent(1, 3));
		assertTrue(DOOR == maze.getContent(13, 12));
	}

	public void printMaze(Maze maze) {
		IntStream.range(0, maze.numRows()).forEach(row -> {
			IntStream.range(0, maze.numCols()).forEach(col -> {
				System.out.print(maze.getContent(col, row));
			});
			System.out.println();
		});
	}
}