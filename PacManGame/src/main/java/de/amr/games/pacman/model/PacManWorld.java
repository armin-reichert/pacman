package de.amr.games.pacman.model;

import static de.amr.games.pacman.model.GameMap.B_EATEN;
import static de.amr.games.pacman.model.GameMap.B_ENERGIZER;
import static de.amr.games.pacman.model.GameMap.B_FOOD;
import static de.amr.games.pacman.model.GameMap.B_INTERSECTION;
import static de.amr.games.pacman.model.GameMap.B_ONE_WAY_DOWN;
import static de.amr.games.pacman.model.GameMap.B_TUNNEL;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import de.amr.easy.game.math.Vector2f;

/**
 * The Pac-Man game world.
 * 
 * @author Armin Reichert
 */
public class PacManWorld {

	public static class Portal {
		public Tile left, right;

		public boolean contains(Tile tile) {
			return tile.equals(left) || tile.equals(right);
		}
	}

	public static final int UNUSED_ROWS_TOP = 4;
	public static final int UNUSED_ROWS_BOTTOM = 3;

	public final int totalFoodCount;
	public final Seat pacManSeat;
	public final Seat ghostSeats[];
	public final Seat bonusSeat;
	public final Portal portal;
	public final Tile horizonNE, horizonNW, horizonSE, horizonSW;
	public final Tile ghostHouseDoorLeft, ghostHouseDoorRight;
	public final Tile cornerNW, cornerNE, cornerSW, cornerSE;

	private final GameMap map;

	public PacManWorld(GameMap map) {
		this.map = map;

		//@formatter:off
		ghostSeats = new Seat[] { 
				new Seat(0, 13, 14, Direction.LEFT), 
				new Seat(1, 11, 17, Direction.UP),
				new Seat(2, 13, 17, Direction.DOWN),
				new Seat(3, 15, 17, Direction.UP),
				};
		//@formatter:on
		pacManSeat = new Seat(4, 13, 26, Direction.RIGHT);
		bonusSeat = new Seat(5, 13, 20, null);

		// scan for portal
		portal = new Portal();
		int portalRow = -1;
		for (int row = 0; row < map.numRows; ++row) {
			if (map.is1(row, 0, B_TUNNEL)) {
				portalRow = row;
				break;
			}
		}
		if (portalRow != -1) {
			portal.left = Tile.xy(-1, portalRow);
			portal.right = Tile.xy(map.numCols, portalRow);
		}

		ghostHouseDoorLeft = Tile.xy(13, 15);
		ghostHouseDoorRight = Tile.xy(14, 15);

		// (unreachable) scattering targets
		horizonNW = Tile.xy(2, 0);
		horizonNE = Tile.xy(25, 0);
		horizonSW = Tile.xy(0, 35);
		horizonSE = Tile.xy(27, 35);

		// only used by algorithm to calculate routes to "safe" corner for fleeing ghosts
		cornerNW = Tile.xy(1, 4);
		cornerNE = Tile.xy(26, 4);
		cornerSW = Tile.xy(1, 32);
		cornerSE = Tile.xy(26, 32);

		totalFoodCount = countFood(map);
	}

	private int countFood(GameMap map) {
		int n = 0;
		for (int row = 0; row < map.numRows; ++row) {
			for (int col = 0; col < map.numCols; ++col) {
				if (map.is1(row, col, B_FOOD)) {
					++n;
				}
			}
		}
		return n;
	}

	public int mapWidth() {
		return map.numCols;
	}

	public int mapHeight() {
		return map.numRows;
	}

	/**
	 * @return Tiles comprising the map only (omitting the areas above and below used for the scores)
	 */
	public Stream<Tile> mapTiles() {
		return IntStream.range(UNUSED_ROWS_TOP * map.numCols, (map.numRows + 1 - UNUSED_ROWS_BOTTOM) * map.numCols)
				.mapToObj(i -> Tile.xy(i % map.numCols, i / map.numCols));
	}

	/**
	 * @param tile reference tile
	 * @param dir  some direction
	 * @param n    number of tiles
	 * @return The tile located <code>n</code> tiles away from the reference tile towards the given
	 *         direction. This can be a tile outside of the map.
	 */
	public Tile tileToDir(Tile tile, Direction dir, int n) {
		if (tile.equals(portal.left) && dir == Direction.LEFT) {
			return portal.right;
		}
		if (tile.equals(portal.right) && dir == Direction.RIGHT) {
			return portal.left;
		}
		Vector2f v = dir.vector();
		return Tile.xy(tile.col + n * v.roundedX(), tile.row + n * v.roundedY());
	}

	/**
	 * @param tile reference tile
	 * @param dir  some direction
	 * @return Neighbor towards the given direction. This can be a tile outside of the map.
	 */
	public Tile neighbor(Tile tile, Direction dir) {
		return tileToDir(tile, dir, 1);
	}

	public void eatAllFood() {
		mapTiles().forEach(this::eatFood);
	}

	public void restoreAllFood() {
		mapTiles().forEach(this::restoreFood);
	}

	public boolean insideMap(Tile tile) {
		return map.inRange(tile.row, tile.col);
	}

	public boolean insideGhostHouse(Tile tile) {
		return isDoor(tile) || tile.inColumnRange(11, 16) && tile.inRowRange(16, 18);
	}

	public boolean atGhostHouseDoor(Tile tile) {
		return isDoor(neighbor(tile, Direction.DOWN));
	}

	public boolean isInaccessible(Tile tile) {
		if (insideMap(tile)) {
			return map.is1(tile.row, tile.col, GameMap.B_WALL);
		}
		return !portal.contains(tile);
	}

	public boolean isTunnel(Tile tile) {
		return insideMap(tile) && map.is1(tile.row, tile.col, B_TUNNEL);
	}

	public boolean isDoor(Tile tile) {
		return tile.equals(ghostHouseDoorLeft) || tile.equals(ghostHouseDoorRight);
	}

	public boolean isOneWayDown(Tile tile) {
		return insideMap(tile) && map.is1(tile.row, tile.col, B_ONE_WAY_DOWN);
	}

	public boolean isIntersection(Tile tile) {
		return insideMap(tile) && map.is1(tile.row, tile.col, B_INTERSECTION);
	}

	public boolean isFood(Tile tile) {
		return insideMap(tile) && map.is1(tile.row, tile.col, B_FOOD);
	}

	public boolean isEatenFood(Tile tile) {
		return insideMap(tile) && map.is1(tile.row, tile.col, B_EATEN);
	}

	public boolean isEnergizer(Tile tile) {
		return insideMap(tile) && map.is1(tile.row, tile.col, B_ENERGIZER);
	}

	public boolean containsSimplePellet(Tile tile) {
		return insideMap(tile) && isFood(tile) && !isEnergizer(tile) && !isEatenFood(tile);
	}

	public boolean containsEnergizer(Tile tile) {
		return insideMap(tile) && map.is1(tile.row, tile.col, B_ENERGIZER) && map.is0(tile.row, tile.col, B_EATEN);
	}

	public boolean containsFood(Tile tile) {
		return insideMap(tile) && map.is0(tile.row, tile.col, B_EATEN) && map.is1(tile.row, tile.col, B_FOOD);
	}

	public boolean containsEatenFood(Tile tile) {
		return insideMap(tile) && map.is1(tile.row, tile.col, B_EATEN) && map.is1(tile.row, tile.col, B_FOOD);
	}

	public void eatFood(Tile tile) {
		if (insideMap(tile)) {
			map.set1(tile.row, tile.col, B_EATEN);
		}
	}

	public void restoreFood(Tile tile) {
		if (insideMap(tile)) {
			map.set0(tile.row, tile.col, B_EATEN);
		}
	}
}