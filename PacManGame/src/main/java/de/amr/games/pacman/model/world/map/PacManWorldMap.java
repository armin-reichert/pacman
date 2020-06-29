package de.amr.games.pacman.model.world.map;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import de.amr.games.pacman.model.Direction;
import de.amr.games.pacman.model.world.Bed;
import de.amr.games.pacman.model.world.Door;
import de.amr.games.pacman.model.world.House;
import de.amr.games.pacman.model.world.OneWayTile;
import de.amr.games.pacman.model.world.Portal;
import de.amr.games.pacman.model.world.Terrain;
import de.amr.games.pacman.model.world.Tile;

/**
 * Base class for maps representing the Pac-Man world.
 * 
 * @author Armin Reichert
 */
public class PacManWorldMap implements Terrain {

	//@formatter:off
	public static final byte B_WALL         = 0;
	public static final byte B_FOOD         = 1;
	public static final byte B_ENERGIZER    = 2;
	public static final byte B_EATEN        = 3;
	public static final byte B_INTERSECTION = 4;
	public static final byte B_TUNNEL       = 5;
	//@formatter:on

	private final byte[][] data;
	protected Bed pacManSeat;
	protected Tile bonusTile;
	protected final List<House> houses = new ArrayList<>();
	protected final List<Portal> portals = new ArrayList<>();
	protected final List<OneWayTile> oneWayTiles = new ArrayList<>();

	public boolean is(int row, int col, byte bit) {
		return (data[row][col] & (1 << bit)) != 0;
	}

	public void set0(int row, int col, byte bit) {
		data[row][col] &= ~(1 << bit);
	}

	public void set1(int row, int col, byte bit) {
		data[row][col] |= (1 << bit);
	}

	public boolean is(Tile tile, byte bit) {
		return contains(tile) && is(tile.row, tile.col, bit);
	}

	public void set(Tile tile, byte bit) {
		if (contains(tile)) {
			set1(tile.row, tile.col, bit);
		}
	}

	public void clear(Tile tile, byte bit) {
		if (contains(tile)) {
			set0(tile.row, tile.col, bit);
		}
	}

	public PacManWorldMap(byte[][] bytes) {
		data = new byte[bytes.length][];
		for (int i = 0; i < bytes.length; ++i) {
			data[i] = Arrays.copyOf(bytes[i], bytes[0].length);
		}
	}

	@Override
	public int width() {
		return data[0].length;
	}

	@Override
	public int height() {
		return data.length;
	}

	@Override
	public boolean contains(Tile tile) {
		return 0 <= tile.row && tile.row < height() && 0 <= tile.col && tile.col < width();
	}

	protected void addPortal(Tile left, Tile right) {
		set0(left.row, left.col, B_WALL);
		set1(left.row, left.col, B_TUNNEL);
		set0(right.row, right.col, B_WALL);
		set1(right.row, right.col, B_TUNNEL);
		portals.add(new Portal(Tile.at(left.col - 1, left.row), Tile.at(right.col + 1, right.row)));
	}

	protected void setEnergizer(Tile tile) {
		set0(tile.row, tile.col, B_WALL);
		set1(tile.row, tile.col, B_FOOD);
		set1(tile.row, tile.col, B_ENERGIZER);
	}

	@Override
	public Stream<House> houses() {
		return houses.stream();
	}

	public Bed pacManSeat() {
		return pacManSeat;
	}

	@Override
	public Tile bonusTile() {
		return bonusTile;
	}

	@Override
	public Stream<Portal> portals() {
		return portals.stream();
	}

	@Override
	public Stream<OneWayTile> oneWayTiles() {
		return oneWayTiles.stream();
	}

	@Override
	public boolean insideHouseOrDoor(Tile tile) {
		return isDoor(tile) || houses().map(House::room).anyMatch(room -> room.contains(tile));
	}

	@Override
	public boolean isAccessible(Tile tile) {
		boolean inside = contains(tile);
		return inside && !is(tile, B_WALL) || !inside && anyPortalContains(tile);
	}

	@Override
	public Tile tileToDir(Tile tile, Direction dir, int n) {
		//@formatter:off
		return portals()
			.filter(portal -> portal.contains(tile))
			.findAny()
			.map(portal -> portal.exitTile(tile, dir))
			.orElse(Tile.at(tile.col + n * dir.vector().roundedX(), tile.row + n * dir.vector().roundedY()));
		//@formatter:on
	}

	@Override
	public Tile neighbor(Tile tile, Direction dir) {
		return tileToDir(tile, dir, 1);
	}

	@Override
	public boolean isIntersection(Tile tile) {
		return is(tile, B_INTERSECTION);
	}

	@Override
	public boolean isDoor(Tile tile) {
		return houses().flatMap(House::doors).anyMatch(door -> door.contains(tile));
	}

	@Override
	public boolean isJustBeforeDoor(Tile tile) {
		for (Direction dir : Direction.values()) {
			Tile neighbor = neighbor(tile, dir);
			if (isDoor(neighbor)) {
				Door door = houses().flatMap(House::doors).filter(d -> d.contains(neighbor)).findFirst().get();
				return door.intoHouse == dir;
			}
		}
		return false;
	}

	@Override
	public boolean isTunnel(Tile tile) {
		return is(tile, B_TUNNEL);
	}
}