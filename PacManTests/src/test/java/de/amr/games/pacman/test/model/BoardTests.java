package de.amr.games.pacman.test.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.amr.games.pacman.model.Maze;

public class BoardTests {

	@Test
	public void testBoardLoading() {
		Maze maze = new Maze();

		assertEquals(28, Maze.COLS);
		assertEquals(36, Maze.ROWS);

		assertNotNull(maze.blinkyHome);
		assertNotNull(maze.blinkyScatter);
		assertNotNull(maze.bonusTile);
		assertNotNull(maze.bottomLeft);
		assertNotNull(maze.bottomRight);
		assertNotNull(maze.clydeHome);
		assertNotNull(maze.clydeScatter);
		assertNotNull(maze.ghostRevival);
		assertNotNull(maze.inkyHome);
		assertNotNull(maze.inkyScatter);
		assertNotNull(maze.pacManHome);
		assertNotNull(maze.pinkyHome);
		assertNotNull(maze.pinkyScatter);
		assertNotNull(maze.tunnelLeftExit);
		assertNotNull(maze.tunnelRightExit);
		assertNotNull(maze.topLeft);
		assertNotNull(maze.topRight);

		assertEquals(4, maze.tiles().filter(tile -> maze.containsEnergizer(tile)).count());
		assertEquals(240, maze.tiles().filter(tile -> maze.containsPellet(tile)).count());

		assertTrue(maze.isWall(maze.tileAt(0, 3)));
		assertTrue(maze.isDoor(maze.tileAt(13, 15)));
		assertTrue(maze.containsPellet(maze.tileAt(1, 4)));
		assertTrue(maze.containsEnergizer(maze.tileAt(1, 6)));

		assertEquals(maze.tileAt(0, 0), maze.tileAt(0, 0));
	}
}