package de.amr.games.pacman.model.world;

import static de.amr.games.pacman.model.world.map.PacManMap.B_EATEN;
import static de.amr.games.pacman.model.world.map.PacManMap.B_ENERGIZER;
import static de.amr.games.pacman.model.world.map.PacManMap.B_FOOD;
import static de.amr.games.pacman.model.world.map.PacManMap.B_INTERSECTION;
import static de.amr.games.pacman.model.world.map.PacManMap.B_TUNNEL;
import static de.amr.games.pacman.model.world.map.PacManMap.B_WALL;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import de.amr.games.pacman.model.Direction;
import de.amr.games.pacman.model.world.map.PacManMap;

/**
 * The Pac-Man game world.
 * 
 * @author Armin Reichert
 */
public class PacManWorld implements PacManWorldStructure {

	private PacManMap map;

	public PacManWorld(PacManMap map) {
		this.map = map;
	}

	@Override
	public int width() {
		return map.width();
	}

	@Override
	public int height() {
		return map.height();
	}

	public boolean insideMap(Tile tile) {
		return 0 <= tile.row && tile.row < height() && 0 <= tile.col && tile.col < width();
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
		return IntStream.range(3 * width(), (height() + 4) * width()).mapToObj(i -> Tile.at(i % width(), i / width()));
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
			.orElse(Tile.at(tile.col + n * dir.vector().roundedX(), tile.row + n * dir.vector().roundedY()));
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
		return insideMap(tile) && map.is(tile.row, tile.col, bit);
	}

	private void set(Tile tile, byte bit) {
		if (insideMap(tile)) {
			map.set1(tile.row, tile.col, bit);
		}
	}

	private void clear(Tile tile, byte bit) {
		if (insideMap(tile)) {
			map.set0(tile.row, tile.col, bit);
		}
	}

	public boolean isAccessible(Tile tile) {
		boolean inside = insideMap(tile);
		return inside && !is(tile, B_WALL) || !inside && anyPortalContains(tile);
	}

	public boolean isTunnel(Tile tile) {
		return is(tile, B_TUNNEL);
	}

	public boolean insidePortal(Tile tile) {
		return map.anyPortalContains(tile);
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