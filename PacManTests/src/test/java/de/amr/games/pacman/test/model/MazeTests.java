package de.amr.games.pacman.test.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import de.amr.games.pacman.controller.actor.PacMan;
import de.amr.games.pacman.model.Game;
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
	public void testMazeStructure() {
		assertNotNull(maze.pacManSeat);
		assertNotNull(maze.ghostSeats[0]);
		assertNotNull(maze.ghostSeats[1]);
		assertNotNull(maze.ghostSeats[2]);
		assertNotNull(maze.ghostSeats[3]);
		assertNotNull(maze.bonusSeat);
		assertNotNull(maze.horizonNE);
		assertNotNull(maze.horizonNW);
		assertNotNull(maze.horizonSW);
		assertNotNull(maze.horizonSE);
		assertTrue(maze.portalLeft.equals(Tile.at(-1, 17)));
		assertTrue(maze.portalRight.equals(Tile.at(28, 17)));
		assertTrue(maze.isWall(Tile.at(0, 3)));
		assertTrue(maze.isDoor(Tile.at(13, 15)));
	}

	@Test
	public void testMazeContent() {
		assertEquals(4, maze.arena().filter(maze::containsEnergizer).count());
		assertEquals(maze.totalFoodCount - 4, maze.arena().filter(maze::containsSimplePellet).count());
		assertTrue(maze.containsSimplePellet(Tile.at(1, 4)));
		assertTrue(maze.containsEnergizer(Tile.at(1, 6)));
	}

	@Test
	public void testTiles() {
		assertEquals(Tile.at(0, 0), Tile.at(0, 0));
		assertNotEquals(Tile.at(0, 0), Tile.at(1, 0));
		assertEquals(4, Tile.at(0, 0).distance(Tile.at(0, 4)), 0);
		assertEquals(4, Tile.at(0, 0).distance(Tile.at(4, 0)), 0);
		assertEquals(Math.sqrt(32), Tile.at(0, 0).distance(Tile.at(4, 4)), 0);
		PacMan pacMan = new PacMan(new Game());
		pacMan.placeAt(Tile.at(-10, 4));
		assertEquals(-10, pacMan.tile().col);
		assertEquals(4, pacMan.tile().row);
	}
}