package de.amr.games.pacman.model;

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

	public final int mapTopRow = 4;
	public final int mapBottomRow = 32;
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
		for (int row = mapTopRow; row <= mapBottomRow; ++row) {
			if (map.isTunnel(row, 0)) {
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

		int foodCount = 0;
		for (int row = mapTopRow; row <= mapBottomRow; ++row) {
			for (int col = 0; col < map.numCols; ++col) {
				if (map.isFood(row, col)) {
					++foodCount;
				}
			}
		}
		totalFoodCount = foodCount;
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
		return IntStream.range(mapTopRow * map.numCols, (mapBottomRow + 1) * map.numCols)
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
		for (int row = mapTopRow; row <= mapBottomRow; ++row) {
			for (int col = 0; col < map.numCols; ++col) {
				if (map.isFood(row, col)) {
					map.eatFood(row, col);
				}
			}
		}
	}

	public void restoreAllFood() {
		for (int row = mapTopRow; row <= mapBottomRow; ++row) {
			for (int col = 0; col < map.numCols; ++col) {
				if (map.isFood(row, col)) {
					map.restoreFood(row, col);
				}
			}
		}
	}

	public boolean insideMap(Tile tile) {
		return map.contains(tile.row, tile.col);
	}

	public boolean insideGhostHouse(Tile tile) {
		return isDoor(tile) || tile.inColumnRange(11, 16) && tile.inRowRange(16, 18);
	}

	public boolean atGhostHouseDoor(Tile tile) {
		return isDoor(neighbor(tile, Direction.DOWN));
	}

	public boolean isInaccessible(Tile tile) {
		if (insideMap(tile)) {
			return map.isWall(tile.row, tile.col);
		}
		return !portal.contains(tile);
	}

	public boolean isTunnel(Tile tile) {
		return insideMap(tile) && map.isTunnel(tile.row, tile.col);
	}

	public boolean isDoor(Tile tile) {
		return tile.equals(ghostHouseDoorLeft) || tile.equals(ghostHouseDoorRight);
	}

	public boolean isOneWayDown(Tile tile) {
		return insideMap(tile) && map.isOneWayDown(tile.row, tile.col);
	}

	public boolean isIntersection(Tile tile) {
		return insideMap(tile) && map.isIntersection(tile.row, tile.col);
	}

	public boolean isFood(Tile tile) {
		return insideMap(tile) && map.isFood(tile.row, tile.col);
	}

	public boolean isEatenFood(Tile tile) {
		return insideMap(tile) && map.isEatenFood(tile.row, tile.col);
	}

	public boolean isEnergizer(Tile tile) {
		return insideMap(tile) && map.isEnergizer(tile.row, tile.col);
	}

	public boolean containsSimplePellet(Tile tile) {
		return insideMap(tile) && isFood(tile) && !isEnergizer(tile) && !isEatenFood(tile);
	}

	public boolean containsEnergizer(Tile tile) {
		return insideMap(tile) && map.containsEnergizer(tile.row, tile.col);
	}

	public boolean containsFood(Tile tile) {
		return insideMap(tile) && map.containsFood(tile.row, tile.col);
	}

	public boolean containsEatenFood(Tile tile) {
		return insideMap(tile) && map.containsEatenFood(tile.row, tile.col);
	}

	public void eatFood(Tile tile) {
		if (insideMap(tile)) {
			map.eatFood(tile.row, tile.col);
		}
	}

}