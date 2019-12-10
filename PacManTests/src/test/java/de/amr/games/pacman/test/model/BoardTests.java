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

		assertEquals(28, Maze.NUM_COLS);
		assertEquals(36, Maze.NUM_ROWS);

		assertNotNull(maze.blinkyHome);
		assertNotNull(maze.scatterTileNE);
		assertNotNull(maze.bonusTile);
		assertNotNull(maze.cornerSW);
		assertNotNull(maze.cornerSE);
		assertNotNull(maze.clydeHome);
		assertNotNull(maze.scatterTileSW);
		assertNotNull(maze.ghostRevival);
		assertNotNull(maze.inkyHome);
		assertNotNull(maze.scatterTileSE);
		assertNotNull(maze.pacManHome);
		assertNotNull(maze.pinkyHome);
		assertNotNull(maze.scatterTileNW);
		assertNotNull(maze.tunnelExitLeft);
		assertNotNull(maze.tunnelExitRight);
		assertNotNull(maze.cornerNW);
		assertNotNull(maze.cornerNE);

		assertEquals(4, maze.tiles().filter(tile -> maze.containsEnergizer(tile)).count());
		assertEquals(240, maze.tiles().filter(tile -> maze.containsPellet(tile)).count());

		assertTrue(maze.isWall(maze.tileAt(0, 3)));
		assertTrue(maze.isDoor(maze.tileAt(13, 15)));
		assertTrue(maze.containsPellet(maze.tileAt(1, 4)));
		assertTrue(maze.containsEnergizer(maze.tileAt(1, 6)));

		assertEquals(maze.tileAt(0, 0), maze.tileAt(0, 0));
	}
}