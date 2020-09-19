package de.amr.games.pacman.model.world.arcade;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Optional;
import java.util.stream.Stream;

import de.amr.games.pacman.model.world.api.Direction;
import de.amr.games.pacman.model.world.api.Food;
import de.amr.games.pacman.model.world.api.TemporaryFood;
import de.amr.games.pacman.model.world.api.Tile;
import de.amr.games.pacman.model.world.components.Bed;
import de.amr.games.pacman.model.world.components.House;
import de.amr.games.pacman.model.world.components.OneWayTile;
import de.amr.games.pacman.model.world.components.Portal;
import de.amr.games.pacman.model.world.components.TiledRectangle;
import de.amr.games.pacman.model.world.core.AbstractTiledWorld;

/**
 * The world of the Arcade version of the game.
 * 
 * @author Armin Reichert
 */
public class ArcadeWorld extends AbstractTiledWorld {

	private static final byte[][] MAP = { // 0 = accessible, 1 = inaccessible, 2 = pellet
			//@formatter:off
			{ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, },
			{ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, },
			{ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, },
			{ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, },
			{ 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, },
			{ 1, 2, 1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 2, 1, 1, 2, 1, 1, 1, 1, 1, 2, 1, 1, 1, 1, 2, 1, },
			{ 1, 2, 1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 2, 1, 1, 2, 1, 1, 1, 1, 1, 2, 1, 1, 1, 1, 2, 1, },
			{ 1, 2, 1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 2, 1, 1, 2, 1, 1, 1, 1, 1, 2, 1, 1, 1, 1, 2, 1, },
			{ 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, },
			{ 1, 2, 1, 1, 1, 1, 2, 1, 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 2, 1, 1, 2, 1, 1, 1, 1, 2, 1, },
			{ 1, 2, 1, 1, 1, 1, 2, 1, 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 2, 1, 1, 2, 1, 1, 1, 1, 2, 1, },
			{ 1, 2, 2, 2, 2, 2, 2, 1, 1, 2, 2, 2, 2, 1, 1, 2, 2, 2, 2, 1, 1, 2, 2, 2, 2, 2, 2, 1, },
			{ 1, 1, 1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 0, 1, 1, 0, 1, 1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 1, },
			{ 1, 1, 1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 0, 1, 1, 0, 1, 1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 1, },
			{ 1, 1, 1, 1, 1, 1, 2, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 2, 1, 1, 1, 1, 1, 1, },
			{ 1, 1, 1, 1, 1, 1, 2, 1, 1, 0, 1, 1, 1, 0, 0, 1, 1, 1, 0, 1, 1, 2, 1, 1, 1, 1, 1, 1, },
			{ 1, 1, 1, 1, 1, 1, 2, 1, 1, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 1, 1, 2, 1, 1, 1, 1, 1, 1, },
			{ 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, },
			{ 1, 1, 1, 1, 1, 1, 2, 1, 1, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 1, 1, 2, 1, 1, 1, 1, 1, 1, },
			{ 1, 1, 1, 1, 1, 1, 2, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 2, 1, 1, 1, 1, 1, 1, },
			{ 1, 1, 1, 1, 1, 1, 2, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 2, 1, 1, 1, 1, 1, 1, },
			{ 1, 1, 1, 1, 1, 1, 2, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 2, 1, 1, 1, 1, 1, 1, },
			{ 1, 1, 1, 1, 1, 1, 2, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 2, 1, 1, 1, 1, 1, 1, },
			{ 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, },
			{ 1, 2, 1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 2, 1, 1, 2, 1, 1, 1, 1, 1, 2, 1, 1, 1, 1, 2, 1, },
			{ 1, 2, 1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 2, 1, 1, 2, 1, 1, 1, 1, 1, 2, 1, 1, 1, 1, 2, 1, },
			{ 1, 2, 2, 2, 1, 1, 2, 2, 2, 2, 2, 2, 2, 0, 0, 2, 2, 2, 2, 2, 2, 2, 1, 1, 2, 2, 2, 1, },
			{ 1, 1, 1, 2, 1, 1, 2, 1, 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 2, 1, 1, 2, 1, 1, 2, 1, 1, 1, },
			{ 1, 1, 1, 2, 1, 1, 2, 1, 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 2, 1, 1, 2, 1, 1, 2, 1, 1, 1, },
			{ 1, 2, 2, 2, 2, 2, 2, 1, 1, 2, 2, 2, 2, 1, 1, 2, 2, 2, 2, 1, 1, 2, 2, 2, 2, 2, 2, 1, },
			{ 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 1, 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 1, },
			{ 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 1, 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 1, },
			{ 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, },
			{ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, },
			{ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, },
			{ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, },
			//@formatter:on
	};

	static final Tile BONUS_LOCATION = Tile.at(13, 20);

	private House house;
	private Bed pacManBed;
	private Portal portal;
	private OneWayTile[] oneWayTiles;
	private TiledRectangle[] tunnels;
	private Tile[] energizerTiles;
	private ArcadeBonus bonus;

	private final BitSet accessible;
	private final BitSet intersections;
	private final BitSet food;
	private final BitSet eaten;
	private int foodCount;

	public ArcadeWorld() {
		super(28, 36);
		accessible = new BitSet(width() * height());
		accessible.set(0, width() * height());
		intersections = new BitSet(width() * height());
		food = new BitSet(width() * height());
		eaten = new BitSet(width() * height());
		for (int row = 0; row < height(); ++row) {
			for (int col = 0; col < width(); ++col) {
				int i = bitIndex(row, col);
				if (MAP[row][col] == 1) {
					accessible.set(i, false);
				} else if (MAP[row][col] == 2) {
					food.set(i, true);
					++foodCount;
				}
			}
		}
		portal = new Portal(Tile.at(0, 17), Tile.at(27, 17), false);
		pacManBed = new Bed(13, 26, Direction.RIGHT);
		//@formatter:off
		house =	House
			.world(this)
			.layout(10, 15, 8, 5)
			.door(Direction.DOWN, 13, 15, 2, 1)
			.bed(13, 14, Direction.LEFT)
			.bed(11, 17, Direction.UP)
			.bed(13, 17, Direction.DOWN)
			.bed(15, 17, Direction.UP)
			.build();

		oneWayTiles = new OneWayTile[] {
			new OneWayTile(12, 13, Direction.DOWN), 
			new OneWayTile(15, 13, Direction.DOWN),
			new OneWayTile(12, 25, Direction.DOWN), 
			new OneWayTile(15, 25, Direction.DOWN)
		};
		
		tunnels = new TiledRectangle[] {
			new TiledRectangle(1, 17, 5, 1),
			new TiledRectangle(22, 17, 5, 1),
		};
		
		energizerTiles = new Tile[] {
			Tile.at(1,6),
			Tile.at(26,6),
			Tile.at(1,26),
			Tile.at(26,26),
		};

		// compute intersections *after* adding house(s)!
		for (int row = 0; row < height(); ++row) {
			for (int col = 0; col < width(); ++col) {
				Tile tile = Tile.at(col, row);
				boolean intersection = Direction.dirs()
					.map(dir -> neighbor(tile, dir))
					.filter(this::isAccessible)
					.filter(this::outsideHouse)
					.count() > 2;
				intersections.set(bitIndex(row, col), intersection);
			}
		}
		//@formatter:on
	}

	private int bitIndex(int row, int col) {
		return row * width() + col;
	}

	private boolean insideWorld(Tile tile) {
		return tile.inColumnRange(0, width() - 1) && tile.inRowRange(0, height() - 1);
	}

	private boolean outsideHouse(Tile tile) {
		return houses().noneMatch(house -> house.includes(tile));
	}

	@Override
	public boolean isAccessible(Tile tile) {
		return insideWorld(tile) && accessible.get(bitIndex(tile.row, tile.col));
	}

	@Override
	public boolean isIntersection(Tile tile) {
		return insideWorld(tile) && intersections.get(bitIndex(tile.row, tile.col));
	}

	@Override
	public int totalFoodCount() {
		return foodCount;
	}

	@Override
	public boolean isTunnel(Tile tile) {
		return Arrays.stream(tunnels).anyMatch(tunnel -> tunnel.includes(tile));
	}

	@Override
	public Stream<House> houses() {
		return Stream.of(house);
	}

	@Override
	public Optional<House> house(int i) {
		return i == 0 ? Optional.of(house) : Optional.empty();
	}

	@Override
	public Bed pacManBed() {
		return pacManBed;
	}

	@Override
	public Stream<Portal> portals() {
		return Stream.of(portal);
	}

	@Override
	public Stream<OneWayTile> oneWayTiles() {
		return Arrays.stream(oneWayTiles);
	}

	@Override
	public void restoreFood() {
		eaten.clear();
	}

	@Override
	public void removeFood(Tile tile) {
		if (insideWorld(tile)) {
			eaten.set(bitIndex(tile.row, tile.col), true);
		}
	}

	@Override
	public boolean hasFood(Tile tile) {
		if (insideWorld(tile)) {
			int i = bitIndex(tile.row, tile.col);
			return food.get(i) && !eaten.get(i);
		}
		return false;
	}

	@Override
	public boolean hasEatenFood(Tile tile) {
		if (insideWorld(tile)) {
			int i = bitIndex(tile.row, tile.col);
			return food.get(i) && eaten.get(i);
		}
		return false;
	}

	@Override
	public void showTemporaryFood(TemporaryFood food) {
		if (!(food instanceof ArcadeBonus)) {
			throw new IllegalArgumentException("Cannot add this type of bonus food to Arcade world");
		}
		bonus = (ArcadeBonus) food;
		bonus.activate();
	}

	@Override
	public void hideTemporaryFood() {
		bonus = null;
	}

	@Override
	public Optional<TemporaryFood> temporaryFood() {
		return Optional.ofNullable(bonus);
	}

	@Override
	public Optional<Food> foodAt(Tile location) {
		if (bonus != null && bonus.location().equals(location)) {
			return Optional.of(bonus);
		}
		if (hasFood(location)) {
			if (Arrays.stream(energizerTiles).anyMatch(energizer -> energizer.equals(location))) {
				return Optional.of(ArcadeFood.ENERGIZER);
			}
			return Optional.of(ArcadeFood.PELLET);
		}
		return Optional.empty();
	}
}