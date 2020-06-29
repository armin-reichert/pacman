package de.amr.games.pacman.test.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import de.amr.games.pacman.controller.actor.PacMan;
import de.amr.games.pacman.model.world.PacManWorldImpl;
import de.amr.games.pacman.model.world.Tile;
import de.amr.games.pacman.model.world.Worlds;

public class WorldTests {

	private PacManWorldImpl world;

	@Before
	public void setup() {
		world = Worlds.arcade();
	}

	@Test
	public void testStructure() {
		assertNotNull(world.pacManSeat());
		assertNotNull(world.theHouse().seat(0));
		assertNotNull(world.theHouse().seat(1));
		assertNotNull(world.theHouse().seat(2));
		assertNotNull(world.theHouse().seat(3));
		assertNotNull(world.bonusTile());
		assertTrue(world.portals().findFirst().get().left.equals(Tile.at(-1, 17)));
		assertTrue(world.portals().findFirst().get().right.equals(Tile.at(28, 17)));
		assertFalse(world.isAccessible(Tile.at(0, 3)));
		assertTrue(world.isDoor(Tile.at(13, 15)));
	}

	@Test
	public void testMazeContent() {
		assertEquals(4, world.mapTiles().filter(world::containsEnergizer).count());
		assertEquals(244, world.mapTiles().filter(world::containsFood).count());
		assertTrue(world.containsSimplePellet(Tile.at(1, 4)));
		assertTrue(world.containsEnergizer(Tile.at(1, 6)));
	}

	@Test
	public void testTiles() {
		assertEquals(Tile.at(0, 0), Tile.at(0, 0));
		assertNotEquals(Tile.at(0, 0), Tile.at(1, 0));
		assertEquals(4, Tile.at(0, 0).distance(Tile.at(0, 4)), 0);
		assertEquals(4, Tile.at(0, 0).distance(Tile.at(4, 0)), 0);
		assertEquals(Math.sqrt(32), Tile.at(0, 0).distance(Tile.at(4, 4)), 0);
		PacMan pacMan = new PacMan();
		pacMan.placeAt(Tile.at(-10, 4));
		assertEquals(-10, pacMan.tile().col);
		assertEquals(4, pacMan.tile().row);
	}
}