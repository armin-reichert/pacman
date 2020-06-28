package de.amr.games.pacman.model.map;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import de.amr.games.pacman.model.world.House;
import de.amr.games.pacman.model.world.OneWayTile;
import de.amr.games.pacman.model.world.PacManWorldStructure;
import de.amr.games.pacman.model.world.Portal;
import de.amr.games.pacman.model.world.Seat;
import de.amr.games.pacman.model.world.Tile;

/**
 * Base class for maps representing the Pac-Man world.
 * 
 * @author Armin Reichert
 */
public class PacManMap implements PacManWorldStructure {

	//@formatter:off
	public static final byte B_WALL         = 0;
	public static final byte B_FOOD         = 1;
	public static final byte B_ENERGIZER    = 2;
	public static final byte B_EATEN        = 3;
	public static final byte B_INTERSECTION = 4;
	public static final byte B_TUNNEL       = 5;
	//@formatter:on

	private final byte[][] data;

	public PacManMap(byte[][] data) {
		this.data = data;
	}

	@Override
	public int width() {
		return data[0].length;
	}

	@Override
	public int height() {
		return data.length;
	}

	public boolean is(int row, int col, byte bit) {
		return (data[row][col] & (1 << bit)) != 0;
	}

	public void set0(int row, int col, byte bit) {
		data[row][col] &= ~(1 << bit);
	}

	public void set1(int row, int col, byte bit) {
		data[row][col] |= (1 << bit);
	}

	protected List<House> houses = new ArrayList<>();
	protected Seat pacManSeat;
	protected Tile bonusTile;
	protected List<Portal> portals = new ArrayList<>();
	protected List<OneWayTile> oneWayTiles = new ArrayList<>();

	protected void addPortal(Tile left, Tile right) {
		set0(left.row, left.col, B_WALL);
		set1(left.row, left.col, B_TUNNEL);
		set0(right.row, right.col, B_WALL);
		set1(right.row, right.col, B_TUNNEL);
		portals.add(new Portal(Tile.at(left.col - 1, left.row), Tile.at(right.col + 1, right.row)));
	}

	@Override
	public Stream<House> houses() {
		return houses.stream();
	}

	@Override
	public Seat pacManSeat() {
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
}