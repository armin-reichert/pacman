package de.amr.games.pacman.test.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import de.amr.games.pacman.controller.actor.PacMan;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.map.GameMaps;
import de.amr.games.pacman.model.world.PacManWorld;
import de.amr.games.pacman.model.world.Tile;

public class MazeTests {

	private PacManWorld world;

	@Before
	public void setup() {
		world = new PacManWorld(GameMaps.ARCADE_MAP);
	}

	@Test
	public void testMazeStructure() {
		assertNotNull(world.pacManSeat());
		assertNotNull(world.ghostSeat(0));
		assertNotNull(world.ghostSeat(1));
		assertNotNull(world.ghostSeat(2));
		assertNotNull(world.ghostSeat(3));
		assertNotNull(world.bonusTile());
		assertNotNull(world.horizonNE);
		assertNotNull(world.horizonNW);
		assertNotNull(world.horizonSW);
		assertNotNull(world.horizonSE);
		assertTrue(world.portals().get(0).left.equals(Tile.col_row(-1, 17)));
		assertTrue(world.portals().get(0).right.equals(Tile.col_row(28, 17)));
		assertTrue(world.isInaccessible(Tile.col_row(0, 3)));
		assertTrue(world.isDoor(Tile.col_row(13, 15)));
	}

	@Test
	public void testMazeContent() {
		assertEquals(4, world.mapTiles().filter(world::containsEnergizer).count());
		assertEquals(world.totalFoodCount - 4, world.mapTiles().filter(world::containsSimplePellet).count());
		assertTrue(world.containsSimplePellet(Tile.col_row(1, 4)));
		assertTrue(world.containsEnergizer(Tile.col_row(1, 6)));
	}

	@Test
	public void testTiles() {
		assertEquals(Tile.col_row(0, 0), Tile.col_row(0, 0));
		assertNotEquals(Tile.col_row(0, 0), Tile.col_row(1, 0));
		assertEquals(4, Tile.col_row(0, 0).distance(Tile.col_row(0, 4)), 0);
		assertEquals(4, Tile.col_row(0, 0).distance(Tile.col_row(4, 0)), 0);
		assertEquals(Math.sqrt(32), Tile.col_row(0, 0).distance(Tile.col_row(4, 4)), 0);
		PacMan pacMan = new PacMan(new Game());
		pacMan.placeAt(Tile.col_row(-10, 4));
		assertEquals(-10, pacMan.tile().col);
		assertEquals(4, pacMan.tile().row);
	}
}