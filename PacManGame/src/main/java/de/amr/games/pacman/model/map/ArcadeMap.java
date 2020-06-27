package de.amr.games.pacman.model.map;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import de.amr.games.pacman.model.Direction;
import de.amr.games.pacman.model.world.Door;
import de.amr.games.pacman.model.world.House;
import de.amr.games.pacman.model.world.OneWayTile;
import de.amr.games.pacman.model.world.Portal;
import de.amr.games.pacman.model.world.Seat;
import de.amr.games.pacman.model.world.Tile;

class ArcadeMap extends PacManMap {

	//@formatter:off
	static final byte[][] DATA = {
		{ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
		{ 1, 2, 2, 2, 2, 2,18, 2, 2, 2, 2, 2, 2, 1, 1, 2, 2, 2, 2, 2, 2,18, 2, 2, 2, 2, 2, 1 },
		{ 1, 2, 1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 2, 1, 1, 2, 1, 1, 1, 1, 1, 2, 1, 1, 1, 1, 2, 1 },
		{ 1, 6, 1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 2, 1, 1, 2, 1, 1, 1, 1, 1, 2, 1, 1, 1, 1, 6, 1 },
		{ 1, 2, 1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 2, 1, 1, 2, 1, 1, 1, 1, 1, 2, 1, 1, 1, 1, 2, 1 },
		{ 1,18, 2, 2, 2, 2,18, 2, 2,18, 2, 2,18, 2, 2,18, 2, 2,18, 2, 2,18, 2, 2, 2, 2,18, 1 },
		{ 1, 2, 1, 1, 1, 1, 2, 1, 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 2, 1, 1, 2, 1, 1, 1, 1, 2, 1 },
		{ 1, 2, 1, 1, 1, 1, 2, 1, 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 2, 1, 1, 2, 1, 1, 1, 1, 2, 1 },
		{ 1, 2, 2, 2, 2, 2,18, 1, 1, 2, 2, 2, 2, 1, 1, 2, 2, 2, 2, 1, 1,18, 2, 2, 2, 2, 2, 1 },
		{ 1, 1, 1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 0, 1, 1, 0, 1, 1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 1 },
		{ 1, 1, 1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 0, 1, 1, 0, 1, 1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 1 },
		{ 1, 1, 1, 1, 1, 1, 2, 1, 1, 0, 0, 0,16, 0, 0,16, 0, 0, 0, 1, 1, 2, 1, 1, 1, 1, 1, 1 },
		{ 1, 1, 1, 1, 1, 1, 2, 1, 1, 0, 1, 1, 1, 0, 0, 1, 1, 1, 0, 1, 1, 2, 1, 1, 1, 1, 1, 1 },
		{ 1, 1, 1, 1, 1, 1, 2, 1, 1, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 1, 1, 2, 1, 1, 1, 1, 1, 1 },
		{32,32,32,32,32,32,18, 0, 0,16, 1, 0, 0, 0, 0, 0, 0, 1,16, 0, 0,18,32,32,32,32,32,32 },
		{ 1, 1, 1, 1, 1, 1, 2, 1, 1, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 1, 1, 2, 1, 1, 1, 1, 1, 1 },
		{ 1, 1, 1, 1, 1, 1, 2, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 2, 1, 1, 1, 1, 1, 1 },
		{ 1, 1, 1, 1, 1, 1, 2, 1, 1,16, 0, 0, 0, 0, 0, 0, 0, 0,16, 1, 1, 2, 1, 1, 1, 1, 1, 1 },
		{ 1, 1, 1, 1, 1, 1, 2, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 2, 1, 1, 1, 1, 1, 1 },
		{ 1, 1, 1, 1, 1, 1, 2, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 2, 1, 1, 1, 1, 1, 1 },
		{ 1, 2, 2, 2, 2, 2,18, 2, 2,18, 2, 2, 2, 1, 1, 2, 2, 2,18, 2, 2,18, 2, 2, 2, 2, 2, 1 },
		{ 1, 2, 1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 2, 1, 1, 2, 1, 1, 1, 1, 1, 2, 1, 1, 1, 1, 2, 1 },
		{ 1, 2, 1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 2, 1, 1, 2, 1, 1, 1, 1, 1, 2, 1, 1, 1, 1, 2, 1 },
		{ 1, 6, 2, 2, 1, 1,18, 2, 2,18, 2, 2,18, 0, 0,18, 2, 2,18, 2, 2,18, 1, 1, 2, 2, 6, 1 },
		{ 1, 1, 1, 2, 1, 1, 2, 1, 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 2, 1, 1, 2, 1, 1, 2, 1, 1, 1 },
		{ 1, 1, 1, 2, 1, 1, 2, 1, 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 2, 1, 1, 2, 1, 1, 2, 1, 1, 1 },
		{ 1, 2, 2,18, 2, 2, 2, 1, 1, 2, 2, 2, 2, 1, 1, 2, 2, 2, 2, 1, 1, 2, 2, 2,18, 2, 2, 1 },
		{ 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 1, 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 1 },
		{ 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 1, 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 1 },
		{ 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,18, 2, 2,18, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1 },
		{ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
	};

	 private final House ghostHouse;
	 private final Seat pacManSeat;
	 private final Tile bonusTile;
	 private final List<Portal> portals;
	 private final List<OneWayTile> oneWayTiles;
	
	//@formatter:on

	public ArcadeMap() {
		super(DATA);

		// ghost house
		int left = 11, right = 16, top = 16, bottom = 18;
		Set<Tile> room = new HashSet<>();
		for (int row = top; row <= bottom; ++row) {
			for (int col = left; col <= right; ++col) {
				room.add(Tile.col_row(col, row));
			}
		}
		Door door = new Door(Direction.DOWN, Tile.col_row(left + 2, top - 1), Tile.col_row(left + 3, top - 1));

		//@formatter:off
		List<Seat> seats = Arrays.asList(
				new Seat(0, left + 2, top - 2, Direction.LEFT),
				new Seat(1, left, top + 1, Direction.UP), 
				new Seat(2, left + 2, top + 1, Direction.DOWN),
				new Seat(3, left + 4, top + 1, Direction.UP));
		//@formatter:on

		ghostHouse = new House(room, Arrays.asList(door), seats);

		pacManSeat = new Seat(4, 13, 26, Direction.RIGHT);
		bonusTile = Tile.col_row(13, 20);

		//@formatter:off
		portals = Arrays.asList(
				new Portal(Tile.col_row(-1, 17), Tile.col_row(28, 17))
		);
		//@formatter:on

		//@formatter:off
		oneWayTiles = Arrays.asList(
				new OneWayTile(12, 13, Direction.DOWN), 
				new OneWayTile(15, 13, Direction.DOWN),
				new OneWayTile(12, 25, Direction.DOWN), 
				new OneWayTile(15, 25, Direction.DOWN)
		);
		//@formatter:on
	}

	@Override
	public int width() {
		return numCols;
	}

	@Override
	public int height() {
		return numRows;
	}

	@Override
	public Stream<House> houses() {
		return Stream.of(ghostHouse);
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