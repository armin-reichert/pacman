package de.amr.games.pacman.test.model;

import static de.amr.games.pacman.model.world.arcade.Pellet.ENERGIZER;
import static de.amr.games.pacman.model.world.arcade.Pellet.SNACK;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import de.amr.games.pacman.controller.creatures.pacman.PacMan;
import de.amr.games.pacman.model.world.api.Direction;
import de.amr.games.pacman.model.world.api.Tile;
import de.amr.games.pacman.model.world.arcade.ArcadeWorld;
import de.amr.games.pacman.model.world.components.Portal;

public class WorldTests {

	private ArcadeWorld world;

	@Before
	public void setup() {
		world = new ArcadeWorld();
	}

	@Test
	public void testStructure() {
		assertEquals(1, world.houses().count());
		assertNotNull(world.pacManBed());
		assertEquals(4, world.house(0).beds().count());
		assertNotNull(world.house(0).bed(0));
		assertNotNull(world.house(0).bed(1));
		assertNotNull(world.house(0).bed(2));
		assertNotNull(world.house(0).bed(3));
		assertEquals(1, world.portals().count());
		assertTrue(world.portals().findFirst().get().either.equals(Tile.at(0, 17)));
		assertTrue(world.portals().findFirst().get().other.equals(Tile.at(27, 17)));
		assertFalse(world.isAccessible(Tile.at(0, 3)));
		assertTrue(world.house(0).hasDoorAt(Tile.at(13, 15)));
	}

	@Test
	public void testMazeContent() {
		assertEquals(4, world.tiles().filter(world::hasFood)
				.filter(location -> world.foodAt(location).equals(Optional.of(ENERGIZER))).count());
		assertEquals(244, world.tiles().filter(world::hasFood).count());
		assertTrue(world.foodAt(Tile.at(1, 4)).get() == SNACK);
		assertTrue(world.foodAt(Tile.at(1, 6)).get() == ENERGIZER);
	}

	@Test
	public void testTiles() {
		assertEquals(Tile.at(0, 0), Tile.at(0, 0));
		assertNotEquals(Tile.at(0, 0), Tile.at(1, 0));
		assertEquals(4, Tile.at(0, 0).distance(Tile.at(0, 4)), 0);
		assertEquals(4, Tile.at(0, 0).distance(Tile.at(4, 0)), 0);
		assertEquals(Math.sqrt(32), Tile.at(0, 0).distance(Tile.at(4, 4)), 0);
		PacMan pacMan = new PacMan(world);
		pacMan.body.placeAt(Tile.at(-10, 4), 0, 0);
		assertEquals(-10, pacMan.body.tile().col);
		assertEquals(4, pacMan.body.tile().row);
	}

	@Test
	public void testPortal() {
		Portal portal = world.portals().findAny().get();
		Tile either = portal.either, other = portal.other;

		// either
		assertEquals(Tile.at(0, 17), either);

		// direct neighbors
		Tile left = Tile.at(other.col, either.row);
		Tile right = Tile.at(1, either.row);
		Tile up = Tile.at(either.col, either.row - 1);
		Tile down = Tile.at(either.col, either.row + 1);

		assertEquals(left, world.tileToDir(either, Direction.LEFT, 1));
		assertEquals(right, world.tileToDir(either, Direction.RIGHT, 1));
		assertEquals(up, world.tileToDir(either, Direction.UP, 1));
		assertEquals(down, world.tileToDir(either, Direction.DOWN, 1));

		assertTrue(world.isAccessible(world.tileToDir(either, Direction.LEFT, 1)));
		assertTrue(world.isAccessible(world.tileToDir(either, Direction.RIGHT, 1)));
		assertTrue(!world.isAccessible(world.tileToDir(either, Direction.UP, 1)));
		assertTrue(!world.isAccessible(world.tileToDir(either, Direction.DOWN, 1)));

		// two tiles away
		assertEquals(world.tileToDir(Tile.at(26, 17), Direction.RIGHT, 2), either);

		// from portal tiles n tiles to the left/right
		for (int d = 1; d < 10; ++d) {
			assertEquals(Tile.at(world.width() - d, either.row), world.tileToDir(either, Direction.LEFT, d));
			assertEquals(Tile.at(d - 1, either.row), world.tileToDir(other, Direction.RIGHT, d));
		}

		// other
		assertEquals(Tile.at(27, 17), other);

		// direct neighbors
		left = Tile.at(other.col - 1, other.row);
		right = Tile.at(either.col, other.row);
		up = Tile.at(other.col, other.row - 1);
		down = Tile.at(other.col, other.row + 1);

		assertEquals(left, world.tileToDir(other, Direction.LEFT, 1));
		assertEquals(right, world.tileToDir(other, Direction.RIGHT, 1));
		assertEquals(up, world.tileToDir(other, Direction.UP, 1));
		assertEquals(down, world.tileToDir(other, Direction.DOWN, 1));

		// two tiles away
		assertEquals(world.tileToDir(Tile.at(1, 17), Direction.LEFT, 2), other);

		// either vs. other
		assertEquals(world.tileToDir(other, Direction.RIGHT, 1), either);
		assertEquals(world.tileToDir(either, Direction.LEFT, 1), other);
	}
}