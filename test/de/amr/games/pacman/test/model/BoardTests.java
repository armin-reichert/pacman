package de.amr.games.pacman.test.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.stream.IntStream;

import org.junit.Test;

import de.amr.easy.game.assets.Assets;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;

public class BoardTests {

	@Test
	public void testBoardLoading() {
		Maze maze = new Maze(Assets.text("maze.txt"));
		printMaze(maze);

		assertEquals(28, maze.numCols());
		assertEquals(31, maze.numRows());

		assertEquals(4, maze.tiles().filter(tile -> maze.isEnergizer(tile)).count());
		assertEquals(240, maze.tiles().filter(tile -> maze.isPellet(tile)).count());

		assertTrue(maze.isWall(new Tile(0, 3)));
		assertTrue(maze.isDoor(new Tile(13, 12)));
		assertTrue(maze.isPellet(new Tile(1, 4)));
		assertTrue(maze.isEnergizer(new Tile(1, 3)));
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