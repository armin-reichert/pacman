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

		assertEquals(28, maze.numCols);
		assertEquals(36, maze.numRows);

		assertNotNull(maze.pacManHome);
		assertNotNull(maze.ghostHouseSeats[0]);
		assertNotNull(maze.ghostHouseSeats[1]);
		assertNotNull(maze.ghostHouseSeats[2]);
		assertNotNull(maze.ghostHouseSeats[3]);
		assertNotNull(maze.portalLeft);
		assertNotNull(maze.portalRight);
		assertNotNull(maze.bonusTile);
		assertNotNull(maze.cornerNW);
		assertNotNull(maze.cornerNE);
		assertNotNull(maze.cornerSW);
		assertNotNull(maze.cornerSE);
		assertNotNull(maze.horizonNE);
		assertNotNull(maze.horizonNW);
		assertNotNull(maze.horizonSW);
		assertNotNull(maze.horizonSE);

		assertEquals(4, maze.tiles().filter(maze::isEnergizer).count());
		assertEquals(240, maze.tiles().filter(maze::isPellet).count());

		assertTrue(maze.isWall(maze.tileAt(0, 3)));
		assertTrue(maze.isDoor(maze.tileAt(13, 15)));
		assertTrue(maze.isPellet(maze.tileAt(1, 4)));
		assertTrue(maze.isEnergizer(maze.tileAt(1, 6)));

		assertEquals(maze.tileAt(0, 0), maze.tileAt(0, 0));
	}
}