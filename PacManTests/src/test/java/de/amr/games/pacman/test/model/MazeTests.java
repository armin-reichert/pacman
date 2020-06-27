package de.amr.games.pacman.test.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import de.amr.games.pacman.controller.actor.PacMan;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.map.PacManMaps;
import de.amr.games.pacman.model.world.PacManWorld;
import de.amr.games.pacman.model.world.Tile;

public class MazeTests {

	private PacManWorld world;

	@Before
	public void setup() {
		world = new PacManWorld(PacManMaps.ARCADE_MAP);
	}

	@Test
	public void testMazeStructure() {
		assertNotNull(world.pacManSeat());
		assertNotNull(world.houses().findFirst().get().seat(0));
		assertNotNull(world.houses().findFirst().get().seat(1));
		assertNotNull(world.houses().findFirst().get().seat(2));
		assertNotNull(world.houses().findFirst().get().seat(3));
		assertNotNull(world.bonusTile());
		assertTrue(world.portals().findFirst().get().left.equals(Tile.at(-1, 17)));
		assertTrue(world.portals().findFirst().get().right.equals(Tile.at(28, 17)));
		assertTrue(world.isInaccessible(Tile.at(0, 3)));
		assertTrue(world.isDoor(Tile.at(13, 15)));
	}

	@Test
	public void testMazeContent() {
		assertEquals(4, world.mapTiles().filter(world::containsEnergizer).count());
		assertEquals(world.totalFoodCount - 4, world.mapTiles().filter(world::containsSimplePellet).count());
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
		PacMan pacMan = new PacMan(new Game());
		pacMan.placeAt(Tile.at(-10, 4));
		assertEquals(-10, pacMan.tile().col);
		assertEquals(4, pacMan.tile().row);
	}
}