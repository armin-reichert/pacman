package de.amr.games.pacman.test.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.model.Tile;

public class BoardTests {

	@Test
	public void testBoardLoading() {
		Maze maze = new Maze(PacManGame.BOARD);

		assertEquals(28, maze.numCols);
		assertEquals(36, maze.numRows);

		assertNotNull(maze.pacManHome);
		assertNotNull(maze.ghostHome[0]);
		assertNotNull(maze.ghostHome[1]);
		assertNotNull(maze.ghostHome[2]);
		assertNotNull(maze.ghostHome[3]);
		assertNotNull(maze.tunnelExitLeft);
		assertNotNull(maze.tunnelExitRight);
		assertNotNull(maze.bonusTile);
		assertNotNull(maze.cornerNW);
		assertNotNull(maze.cornerNE);
		assertNotNull(maze.cornerSW);
		assertNotNull(maze.cornerSE);
		assertNotNull(maze.scatterNE);
		assertNotNull(maze.scatterNW);
		assertNotNull(maze.scatterSW);
		assertNotNull(maze.scatterSE);

		assertEquals(4, maze.tiles().filter(Tile::containsEnergizer).count());
		assertEquals(240, maze.tiles().filter(Tile::containsPellet).count());

		assertTrue(maze.tileAt(0, 3).isWall());
		assertTrue(maze.tileAt(13, 15).isDoor());
		assertTrue(maze.tileAt(1, 4).containsPellet());
		assertTrue(maze.tileAt(1, 6).containsEnergizer());

		assertEquals(maze.tileAt(0, 0), maze.tileAt(0, 0));
	}
}