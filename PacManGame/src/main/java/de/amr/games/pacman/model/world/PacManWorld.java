package de.amr.games.pacman.model.world;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import de.amr.easy.game.math.Vector2f;
import de.amr.games.pacman.model.Direction;
import de.amr.games.pacman.model.map.GameMap;

/**
 * The Pac-Man game world. Reserves 3 rows above and 2 rows below the map for displaying the scores
 * and counters.
 * 
 * @author Armin Reichert
 */
public class PacManWorld {

	static final int ROWS_ABOVE_MAP = 3;
	static final int ROWS_BELOW_MAP = 2;

	static int toWorld(int row) {
		return row + ROWS_ABOVE_MAP;
	}

	static int toMap(int row) {
		return row - ROWS_ABOVE_MAP;
	}

	//@formatter:off
	static final byte B_WALL         = 0;
	static final byte B_FOOD         = 1;
	static final byte B_ENERGIZER    = 2;
	static final byte B_EATEN        = 3;
	static final byte B_INTERSECTION = 4;
	static final byte B_ONE_WAY_DOWN = 5;
	static final byte B_TUNNEL       = 6;
	//@formatter:on

	public final int totalFoodCount;
	public final Tile horizonNE, horizonNW, horizonSE, horizonSW;
	public final Tile cornerNW, cornerNE, cornerSW, cornerSE;

	private final GameMap map;

	public PacManWorld(GameMap map) {
		this.map = map;
		totalFoodCount = (int) mapTiles().filter(this::isFood).count();

		// (unreachable) scattering targets
		horizonNW = Tile.col_row(2, 0);
		horizonNE = Tile.col_row(width() - 3, 0);
		horizonSW = Tile.col_row(0, height() - 1);
		horizonSE = Tile.col_row(width() - 1, height() - 1);

		// only used by algorithm to calculate routes to "safe" corner for fleeing ghosts
		cornerNW = Tile.col_row(1, 4);
		cornerNE = Tile.col_row(26, 4);
		cornerSW = Tile.col_row(1, 32);
		cornerSE = Tile.col_row(26, 32);
	}

	public int width() {
		return map.numCols;
	}

	public int height() {
		return ROWS_ABOVE_MAP + map.numRows + ROWS_BELOW_MAP;
	}

	public GhostHouse ghostHouse() {
		return map.ghostHouse();
	}

	public Seat pacManSeat() {
		return map.pacManSeat();
	}

	public Tile bonusTile() {
		return map.bonusTile();
	}

	public List<Portal> portals() {
		return map.portals();
	}

	/**
	 * @return the map tiles in world coordinates
	 */
	public Stream<Tile> mapTiles() {
		return IntStream.range(toWorld(0) * map.numCols, toWorld(map.numRows + 1) * map.numCols)
				.mapToObj(i -> Tile.col_row(i % map.numCols, i / map.numCols));
	}

	/**
	 * @param tile reference tile
	 * @param dir  some direction
	 * @param n    number of tiles
	 * @return The tile located <code>n</code> tiles away from the reference tile towards the given
	 *         direction. This can be a tile outside of the world.
	 */
	public Tile tileToDir(Tile tile, Direction dir, int n) {
		Optional<Portal> portalEntered = portals().stream().filter(portal -> portal.contains(tile)).findAny();
		if (portalEntered.isPresent()) {
			Tile exitTile = portalEntered.get().exitTile(tile, dir);
			if (exitTile != null) {
				return exitTile;
			}
		}
		Vector2f v = dir.vector();
		return Tile.col_row(tile.col + n * v.roundedX(), tile.row + n * v.roundedY());
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
		return map.contains(toMap(tile.row), tile.col);
	}

	public boolean isDoor(Tile tile) {
		return ghostHouse().doors().stream().anyMatch(door -> door.contains(tile));
	}

	public boolean insideGhostHouse(Tile tile) {
		return ghostHouse().room().contains(tile) || isDoor(tile);
	}

	public boolean atGhostHouseDoor(Tile tile) {
		return isDoor(neighbor(tile, Direction.DOWN));
	}

	public boolean isInaccessible(Tile tile) {
		if (insideMap(tile)) {
			return map.is1(toMap(tile.row), tile.col, B_WALL);
		}
		boolean isPortal = portals().stream().anyMatch(portal -> portal.contains(tile));
		return !isPortal;
	}

	public boolean isTunnel(Tile tile) {
		return insideMap(tile) && map.is1(toMap(tile.row), tile.col, B_TUNNEL);
	}

	public boolean isOneWayDown(Tile tile) {
		return insideMap(tile) && map.is1(toMap(tile.row), tile.col, B_ONE_WAY_DOWN);
	}

	public boolean isIntersection(Tile tile) {
		return insideMap(tile) && map.is1(toMap(tile.row), tile.col, B_INTERSECTION);
	}

	public boolean isFood(Tile tile) {
		return insideMap(tile) && map.is1(toMap(tile.row), tile.col, B_FOOD);
	}

	public boolean isEatenFood(Tile tile) {
		return insideMap(tile) && map.is1(toMap(tile.row), tile.col, B_EATEN);
	}

	public boolean isEnergizer(Tile tile) {
		return insideMap(tile) && map.is1(toMap(tile.row), tile.col, B_ENERGIZER);
	}

	public boolean containsSimplePellet(Tile tile) {
		return insideMap(tile) && isFood(tile) && !isEnergizer(tile) && !isEatenFood(tile);
	}

	public boolean containsEnergizer(Tile tile) {
		return insideMap(tile) && map.is1(toMap(tile.row), tile.col, B_ENERGIZER)
				&& map.is0(toMap(tile.row), tile.col, B_EATEN);
	}

	public boolean containsFood(Tile tile) {
		return insideMap(tile) && map.is0(toMap(tile.row), tile.col, B_EATEN) && map.is1(toMap(tile.row), tile.col, B_FOOD);
	}

	public boolean containsEatenFood(Tile tile) {
		return insideMap(tile) && map.is1(toMap(tile.row), tile.col, B_EATEN) && map.is1(toMap(tile.row), tile.col, B_FOOD);
	}

	public void eatFood(Tile tile) {
		if (insideMap(tile)) {
			map.set1(toMap(tile.row), tile.col, B_EATEN);
		}
	}

	public void restoreFood(Tile tile) {
		if (insideMap(tile)) {
			map.set0(toMap(tile.row), tile.col, B_EATEN);
		}
	}
}