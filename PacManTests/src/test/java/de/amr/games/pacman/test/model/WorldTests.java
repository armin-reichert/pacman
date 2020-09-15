package de.amr.games.pacman.test.model;

import static de.amr.games.pacman.model.world.arcade.ArcadeFood.ENERGIZER;
import static de.amr.games.pacman.model.world.arcade.ArcadeFood.PELLET;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;

import de.amr.games.pacman.controller.creatures.pacman.PacMan;
import de.amr.games.pacman.model.world.api.Direction;
import de.amr.games.pacman.model.world.arcade.ArcadeWorld;
import de.amr.games.pacman.model.world.components.Block;
import de.amr.games.pacman.model.world.components.House;
import de.amr.games.pacman.model.world.components.Portal;
import de.amr.games.pacman.model.world.components.Tile;

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
		House house = world.house(0).get();
		assertEquals(4, house.beds().count());
		assertNotNull(house.bed(0));
		assertNotNull(house.bed(1));
		assertNotNull(house.bed(2));
		assertNotNull(house.bed(3));
		assertEquals(1, world.portals().count());
		assertTrue(world.portals().findFirst().get().either.equals(Tile.at(0, 17)));
		assertTrue(world.portals().findFirst().get().other.equals(Tile.at(27, 17)));
		assertFalse(world.isAccessible(Tile.at(0, 3)));
		assertTrue(house.hasDoorAt(Tile.at(13, 15)));

		/*
		 * Intersection tiles: (6,4) (21,4) (1,8) (6,8) (9,8) (12,8) (15,8) (18,8) (21,8) (26,8) (6,11)
		 * (21,11) (12,14) (15,14) (6,17) (9,17) (18,17) (21,17) (9,20) (18,20) (6,23) (9,23) (18,23)
		 * (21,23) (6,26) (9,26) (12,26) (15,26) (18,26) (21,26) (3,29) (24,29) (12,32) (15,32)
		 */
		assertTrue(world.isIntersection(Tile.at(1, 8)));
		assertTrue(world.isIntersection(Tile.at(21, 23)));
		assertTrue(world.isIntersection(Tile.at(9, 17)));
		assertTrue(world.isIntersection(Tile.at(12, 32)));
		assertTrue(world.isIntersection(Tile.at(15, 32)));

		world.tiles().filter(world::isIntersection).forEach(System.out::println);
	}

	@Test
	public void testMazeContent() {
		assertEquals(4, world.tiles().filter(world::hasFood)
				.filter(location -> world.foodAt(location).equals(Optional.of(ENERGIZER))).count());
		assertEquals(244, world.tiles().filter(world::hasFood).count());
		assertTrue(world.foodAt(Tile.at(1, 4)).get() == PELLET);
		assertTrue(world.foodAt(Tile.at(1, 6)).get() == ENERGIZER);
	}

	@Test
	public void testTiles() {
		assertEquals(Tile.at(0, 0), Tile.at(0, 0));
		assertNotEquals(Tile.at(0, 0), Tile.at(1, 0));
		assertEquals(4, Tile.at(0, 0).distance(Tile.at(0, 4)), 0);
		assertEquals(4, Tile.at(0, 0).distance(Tile.at(4, 0)), 0);
		assertEquals(Math.sqrt(32), Tile.at(0, 0).distance(Tile.at(4, 4)), 0);
		PacMan pacMan = new PacMan(world, "Pac-Man-Dummy");
		pacMan.placeAt(Tile.at(-10, 4), 0, 0);
		assertEquals(-10, pacMan.tile().col);
		assertEquals(4, pacMan.tile().row);
	}

	@Test
	public void testTilesIterator() {
		Block block = new Block(1, 1, 2, 5);
		List<Tile> tiles = block.tiles().collect(Collectors.toList());
		assertEquals(List.of(Tile.at(1, 1), Tile.at(2, 1), Tile.at(1, 2), Tile.at(2, 2), Tile.at(1, 3), Tile.at(2, 3),
				Tile.at(1, 4), Tile.at(2, 4), Tile.at(1, 5), Tile.at(2, 5)), tiles);
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