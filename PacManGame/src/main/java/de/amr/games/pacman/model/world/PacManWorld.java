package de.amr.games.pacman.model.world;

import static de.amr.games.pacman.model.map.PacManMap.B_EATEN;
import static de.amr.games.pacman.model.map.PacManMap.B_ENERGIZER;
import static de.amr.games.pacman.model.map.PacManMap.B_FOOD;
import static de.amr.games.pacman.model.map.PacManMap.B_INTERSECTION;
import static de.amr.games.pacman.model.map.PacManMap.B_TUNNEL;
import static de.amr.games.pacman.model.map.PacManMap.B_WALL;

import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import de.amr.easy.game.math.Vector2f;
import de.amr.games.pacman.model.Direction;
import de.amr.games.pacman.model.map.PacManMap;

/**
 * The Pac-Man game world. Reserves 3 rows above and 2 rows below the map for displaying the scores
 * and counters.
 * 
 * @author Armin Reichert
 */
public class PacManWorld implements PacManWorldStructure {

	static final int ROWS_ABOVE_MAP = 3;
	static final int ROWS_BELOW_MAP = 2;

	static int toWorld(int row) {
		return row + ROWS_ABOVE_MAP;
	}

	static int toMap(int row) {
		return row - ROWS_ABOVE_MAP;
	}

	public final int totalFoodCount;
	public final Tile cornerNW, cornerNE, cornerSW, cornerSE;

	private final PacManMap map;

	public PacManWorld(PacManMap map) {
		this.map = map;
		totalFoodCount = (int) mapTiles().filter(this::isFood).count();

		// only used by algorithm to calculate routes to "safe" corner for fleeing ghosts
		cornerNW = Tile.at(1, ROWS_ABOVE_MAP + 1);
		cornerNE = Tile.at(width() - 2, ROWS_ABOVE_MAP + 1);
		cornerSW = Tile.at(1, toWorld(map.numRows - 2));
		cornerSE = Tile.at(width() - 2, toWorld(map.numRows - 2));
	}

	@Override
	public int width() {
		return map.numCols;
	}

	@Override
	public int height() {
		return ROWS_ABOVE_MAP + map.numRows + ROWS_BELOW_MAP;
	}

	@Override
	public Stream<House> houses() {
		return map.houses();
	}

	@Override
	public Seat pacManSeat() {
		return map.pacManSeat();
	}

	@Override
	public Tile bonusTile() {
		return map.bonusTile();
	}

	@Override
	public Stream<Portal> portals() {
		return map.portals();
	}

	@Override
	public Stream<OneWayTile> oneWayTiles() {
		return map.oneWayTiles();
	}

	/**
	 * @return the map tiles in world coordinates
	 */
	public Stream<Tile> mapTiles() {
		return IntStream.range(toWorld(0) * map.numCols, toWorld(map.numRows + 1) * map.numCols)
				.mapToObj(i -> Tile.at(i % map.numCols, i / map.numCols));
	}

	/**
	 * @param tile reference tile
	 * @param dir  some direction
	 * @param n    number of tiles
	 * @return The tile located <code>n</code> tiles away from the reference tile towards the given
	 *         direction. This can be a tile outside of the world.
	 */
	public Tile tileToDir(Tile tile, Direction dir, int n) {
		Optional<Portal> maybePortal = portals().filter(portal -> portal.contains(tile)).findAny();
		if (maybePortal.isPresent()) {
			Tile exitTile = maybePortal.get().exitTile(tile, dir);
			if (exitTile != null) {
				return exitTile;
			}
		}
		Vector2f v = dir.vector();
		return Tile.at(tile.col + n * v.roundedX(), tile.row + n * v.roundedY());
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
		return houses().flatMap(House::doors).anyMatch(door -> door.contains(tile));
	}

	public boolean insideGhostHouse(Tile tile) {
		return houses().map(House::room).anyMatch(room -> room.contains(tile) || isDoor(tile));
	}

	public boolean atGhostHouseDoor(Tile tile) {
		return isDoor(neighbor(tile, Direction.DOWN));
	}

	public boolean isInaccessible(Tile tile) {
		if (insideMap(tile)) {
			return map.is1(toMap(tile.row), tile.col, B_WALL);
		}
		boolean isPortal = portals().anyMatch(portal -> portal.contains(tile));
		return !isPortal;
	}

	public boolean isTunnel(Tile tile) {
		return insideMap(tile) && map.is1(toMap(tile.row), tile.col, B_TUNNEL);
	}

	public boolean isOneWayTile(Tile tile) {
		return insideMap(tile) && map.oneWayTiles().anyMatch(oneWay -> oneWay.tile.equals(tile));
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