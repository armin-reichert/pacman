package de.amr.games.pacman.test.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;

public class BoardTests {

	@Test
	public void testBoardLoading() {
		Maze maze = new Maze();

		assertEquals(28, maze.numCols());
		assertEquals(31, maze.numRows());

		assertEquals(4, maze.tiles().filter(tile -> maze.isEnergizer(tile)).count());
		assertEquals(240, maze.tiles().filter(tile -> maze.isPellet(tile)).count());

		assertTrue(maze.isWall(new Tile(0, 3)));
		assertTrue(maze.isDoor(new Tile(13, 12)));
		assertTrue(maze.isPellet(new Tile(1, 4)));
		assertTrue(maze.isEnergizer(new Tile(1, 3)));
	}
}