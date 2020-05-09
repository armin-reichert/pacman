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
		assertEquals(240, maze.tiles().filter(maze::isSimplePellet).count());

		assertTrue(maze.isWall(new Tile(0, 3)));
		assertTrue(maze.isDoor(new Tile(13, 15)));
		assertTrue(maze.isSimplePellet(new Tile(1, 4)));
		assertTrue(maze.isEnergizer(new Tile(1, 6)));
		assertTrue(maze.portalLeft.equals(new Tile(-1,17)));
		assertTrue(maze.portalRight.equals(new Tile(28,17)));

		assertEquals(new Tile(0, 0), new Tile(0, 0));
	}
}