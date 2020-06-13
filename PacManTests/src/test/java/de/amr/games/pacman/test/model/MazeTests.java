package de.amr.games.pacman.test.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;

public class MazeTests {

	private Maze maze;

	@Before
	public void setup() {
		maze = new Maze();
	}

	@Test
	public void testMazeSize() {
		assertEquals(28, maze.numCols);
		assertEquals(36, maze.numRows);
	}

	@Test
	public void testMazeContent() {
		assertNotNull(maze.pacManHome);
		assertNotNull(maze.ghostHome[0]);
		assertNotNull(maze.ghostHome[1]);
		assertNotNull(maze.ghostHome[2]);
		assertNotNull(maze.ghostHome[3]);
		assertNotNull(maze.bonusTile);
		assertNotNull(maze.horizonNE);
		assertNotNull(maze.horizonNW);
		assertNotNull(maze.horizonSW);
		assertNotNull(maze.horizonSE);

		assertEquals(4, maze.playingArea.stream().filter(maze::containsEnergizer).count());
		assertEquals(240, maze.playingArea.stream().filter(maze::containsSimplePellet).count());

		assertTrue(maze.isWall(new Tile(0, 3)));
		assertTrue(maze.isDoor(new Tile(13, 15)));
		assertTrue(maze.containsSimplePellet(new Tile(1, 4)));
		assertTrue(maze.containsEnergizer(new Tile(1, 6)));
		assertTrue(maze.portalLeft.equals(new Tile(-1, 17)));
		assertTrue(maze.portalRight.equals(new Tile(28, 17)));
	}

	@Test
	public void testTiles() {
		assertEquals(new Tile(0, 0), new Tile(0, 0));
		assertNotEquals(new Tile(0, 0), new Tile(1, 0));
		assertEquals(4, new Tile(0, 0).distance(new Tile(0, 4)), 0);
		assertEquals(4, new Tile(0, 0).distance(new Tile(4, 0)), 0);
		assertEquals(Math.sqrt(32), new Tile(0, 0).distance(new Tile(4, 4)), 0);
	}
}