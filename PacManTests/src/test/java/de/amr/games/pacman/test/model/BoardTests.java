package de.amr.games.pacman.test.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.amr.games.pacman.model.Maze;

public class BoardTests {

	@Test
	public void testBoardLoading() {
		Maze maze = new Maze();

		assertEquals(28, maze.numCols());
		assertEquals(36, maze.numRows());

		assertEquals(4, maze.tiles().filter(tile -> maze.containsEnergizer(tile)).count());
		assertEquals(240, maze.tiles().filter(tile -> maze.containsPellet(tile)).count());

		assertTrue(maze.isWall(maze.tileAt(0, 3)));
		assertTrue(maze.isDoor(maze.tileAt(13, 15)));
		assertTrue(maze.containsPellet(maze.tileAt(1, 4)));
		assertTrue(maze.containsEnergizer(maze.tileAt(1, 6)));

		assertEquals(maze.tileAt(0, 0), maze.tileAt(0, 0));
	}
}