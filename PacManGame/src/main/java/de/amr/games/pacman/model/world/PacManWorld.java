package de.amr.games.pacman.model.world;

import static de.amr.games.pacman.model.map.PacManMap.B_EATEN;
import static de.amr.games.pacman.model.map.PacManMap.B_ENERGIZER;
import static de.amr.games.pacman.model.map.PacManMap.B_FOOD;
import static de.amr.games.pacman.model.map.PacManMap.B_INTERSECTION;
import static de.amr.games.pacman.model.map.PacManMap.B_TUNNEL;
import static de.amr.games.pacman.model.map.PacManMap.B_WALL;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import de.amr.games.pacman.model.Direction;
import de.amr.games.pacman.model.map.PacManMap;

/**
 * The Pac-Man game world. Adds 3 rows above and 2 rows below the map for displaying the scores and
 * counters.
 * 
 * @author Armin Reichert
 */
public class PacManWorld implements PacManWorldStructure {

	private static final int ROWS_ABOVE_MAP = 3;
	private static final int ROWS_BELOW_MAP = 2;

	private static int toMap(int row) {
		return row - ROWS_ABOVE_MAP;
	}

	public final Tile cornerNW, cornerNE, cornerSW, cornerSE;

	private final PacManMap map;

	public PacManWorld(PacManMap map) {
		this.map = map;
		// inside corners, assume wall layer ot thickness 1 around maze
		int left = 1, right = map.width() - 2, top = ROWS_ABOVE_MAP + 1, bottom = ROWS_ABOVE_MAP + map.height() - 2;
		cornerNW = Tile.at(left, top);
		cornerNE = Tile.at(right, top);
		cornerSW = Tile.at(left, bottom);
		cornerSE = Tile.at(right, bottom);
	}

	@Override
	public int width() {
		return map.width();
	}

	@Override
	public int height() {
		return ROWS_ABOVE_MAP + map.height() + ROWS_BELOW_MAP;
	}

	public boolean insideMap(Tile tile) {
		int mapRow = toMap(tile.row), mapCol = tile.col;
		return 0 <= mapRow && mapRow < map.height() && 0 <= mapCol && mapCol < map.width();
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
		return IntStream.range(ROWS_ABOVE_MAP * map.width(), (ROWS_ABOVE_MAP + map.height() + 1) * map.width())
				.mapToObj(i -> Tile.at(i % map.width(), i / map.width()));
	}

	/**
	 * @param tile reference tile
	 * @param dir  some direction
	 * @param n    number of tiles
	 * @return The tile located <code>n</code> tiles away from the reference tile towards the given
	 *         direction. This can be a tile outside of the world.
	 */
	public Tile tileToDir(Tile tile, Direction dir, int n) {
		//@formatter:off
		return portals()
				.filter(portal -> portal.contains(tile))
				.findAny()
				.map(portal -> portal.exitTile(tile, dir))
				.orElse(
						Tile.at(tile.col + n * dir.vector().roundedX(), tile.row + n * dir.vector().roundedY())
				);
		//@formatter:on
	}

	/**
	 * @param tile reference tile
	 * @param dir  some direction
	 * @return Neighbor towards the given direction. This can be a tile outside of the map.
	 */
	public Tile neighbor(Tile tile, Direction dir) {
		return tileToDir(tile, dir, 1);
	}

	public Stream<Tile> neighbors(Tile tile) {
		return Direction.dirs().map(dir -> neighbor(tile, dir));
	}

	public void eatFood() {
		mapTiles().forEach(this::eatFood);
	}

	public void restoreFood() {
		mapTiles().forEach(this::restoreFood);
	}

	public boolean isDoor(Tile tile) {
		return houses().flatMap(House::doors).anyMatch(door -> door.contains(tile));
	}

	public boolean insideHouseOrDoor(Tile tile) {
		return isDoor(tile) || houses().map(House::room).anyMatch(room -> room.contains(tile));
	}

	public boolean outsideAtDoor(Tile tile) {
		for (Direction dir : Direction.values()) {
			Tile neighbor = neighbor(tile, dir);
			if (isDoor(neighbor)) {
				Door door = houses().flatMap(House::doors).filter(d -> d.contains(neighbor)).findFirst().get();
				return door.intoHouse == dir;
			}
		}
		return false;
	}

	private boolean is(Tile tile, byte bit) {
		return insideMap(tile) && map.is(toMap(tile.row), tile.col, bit);
	}

	private void set(Tile tile, byte bit) {
		if (insideMap(tile)) {
			map.set1(toMap(tile.row), tile.col, bit);
		}
	}

	private void clear(Tile tile, byte bit) {
		if (insideMap(tile)) {
			map.set0(toMap(tile.row), tile.col, bit);
		}
	}

	public boolean isInaccessible(Tile tile) {
		boolean inside = insideMap(tile);
		return inside && is(tile, B_WALL) || !inside && !isPortal(tile);
	}

	public boolean isTunnel(Tile tile) {
		return is(tile, B_TUNNEL);
	}

	public boolean isIntersection(Tile tile) {
		return is(tile, B_INTERSECTION);
	}

	public boolean containsFood(Tile tile) {
		return is(tile, B_FOOD) && !is(tile, B_EATEN);
	}

	public boolean containsEatenFood(Tile tile) {
		return is(tile, B_FOOD) && is(tile, B_EATEN);
	}

	public boolean isEnergizer(Tile tile) {
		return is(tile, B_ENERGIZER);
	}

	public boolean containsSimplePellet(Tile tile) {
		return containsFood(tile) && !isEnergizer(tile);
	}

	public boolean containsEnergizer(Tile tile) {
		return containsFood(tile) && isEnergizer(tile);
	}

	public void eatFood(Tile tile) {
		if (is(tile, B_FOOD)) {
			set(tile, B_EATEN);
		}
	}

	public void restoreFood(Tile tile) {
		if (is(tile, B_FOOD)) {
			clear(tile, B_EATEN);
		}
	}
}