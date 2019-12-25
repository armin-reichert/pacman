package de.amr.games.pacman.test.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;

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
		assertNotNull(maze.tunnelExitLeft);
		assertNotNull(maze.tunnelExitRight);
		assertNotNull(maze.bonusTile);
		assertNotNull(maze.cornerNW);
		assertNotNull(maze.cornerNE);
		assertNotNull(maze.cornerSW);
		assertNotNull(maze.cornerSE);
		assertNotNull(maze.horizonNE);
		assertNotNull(maze.horizonNW);
		assertNotNull(maze.horizonSW);
		assertNotNull(maze.horizonSE);

		assertEquals(4, maze.tiles().filter(Tile::containsEnergizer).count());
		assertEquals(240, maze.tiles().filter(Tile::containsPellet).count());

		assertTrue(maze.tileAt(0, 3).isWall());
		assertTrue(maze.isDoor(maze.tileAt(13, 15)));
		assertTrue(maze.tileAt(1, 4).containsPellet());
		assertTrue(maze.tileAt(1, 6).containsEnergizer());

		assertEquals(maze.tileAt(0, 0), maze.tileAt(0, 0));
	}
}