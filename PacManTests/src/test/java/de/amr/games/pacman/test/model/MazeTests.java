package de.amr.games.pacman.test.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import de.amr.games.pacman.controller.actor.PacMan;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.model.map.GameMaps;
import de.amr.games.pacman.model.world.PacManWorld;

public class MazeTests {

	private PacManWorld world;

	@Before
	public void setup() {
		world = new PacManWorld(GameMaps.ARCADE_MAP);
	}

	@Test
	public void testMazeStructure() {
		assertNotNull(world.pacManSeat);
		assertNotNull(world.ghostSeat(0));
		assertNotNull(world.ghostSeat(1));
		assertNotNull(world.ghostSeat(2));
		assertNotNull(world.ghostSeat(3));
		assertNotNull(world.bonusSeat);
		assertNotNull(world.horizonNE);
		assertNotNull(world.horizonNW);
		assertNotNull(world.horizonSW);
		assertNotNull(world.horizonSE);
		assertTrue(world.portal.left.equals(Tile.xy(-1, 17)));
		assertTrue(world.portal.right.equals(Tile.xy(28, 17)));
		assertTrue(world.isInaccessible(Tile.xy(0, 3)));
		assertTrue(world.isDoor(Tile.xy(13, 15)));
	}

	@Test
	public void testMazeContent() {
		assertEquals(4, world.mapTiles().filter(world::containsEnergizer).count());
		assertEquals(world.totalFoodCount - 4, world.mapTiles().filter(world::containsSimplePellet).count());
		assertTrue(world.containsSimplePellet(Tile.xy(1, 4)));
		assertTrue(world.containsEnergizer(Tile.xy(1, 6)));
	}

	@Test
	public void testTiles() {
		assertEquals(Tile.xy(0, 0), Tile.xy(0, 0));
		assertNotEquals(Tile.xy(0, 0), Tile.xy(1, 0));
		assertEquals(4, Tile.xy(0, 0).distance(Tile.xy(0, 4)), 0);
		assertEquals(4, Tile.xy(0, 0).distance(Tile.xy(4, 0)), 0);
		assertEquals(Math.sqrt(32), Tile.xy(0, 0).distance(Tile.xy(4, 4)), 0);
		PacMan pacMan = new PacMan(new Game());
		pacMan.placeAt(Tile.xy(-10, 4));
		assertEquals(-10, pacMan.tile().col);
		assertEquals(4, pacMan.tile().row);
	}
}