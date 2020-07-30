package de.amr.games.pacman.model.world.arcade;

import static de.amr.games.pacman.model.world.core.WorldMap.B_ENERGIZER;
import static de.amr.games.pacman.model.world.core.WorldMap.B_FOOD;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import de.amr.games.pacman.model.world.api.BonusFood;
import de.amr.games.pacman.model.world.api.Direction;
import de.amr.games.pacman.model.world.api.Food;
import de.amr.games.pacman.model.world.api.Tile;
import de.amr.games.pacman.model.world.components.Bed;
import de.amr.games.pacman.model.world.components.Door;
import de.amr.games.pacman.model.world.components.House;
import de.amr.games.pacman.model.world.components.HouseBuilder;
import de.amr.games.pacman.model.world.components.OneWayTile;
import de.amr.games.pacman.model.world.components.Portal;
import de.amr.games.pacman.model.world.core.MapBasedWorld;

/**
 * Map-based Pac-Man game world implementation.
 * 
 * @author Armin Reichert
 */
public class ArcadeWorld extends MapBasedWorld {

	static final byte[][] DATA = {
			//@formatter:off
			{ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
			{ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
			{ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
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
			{ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
			{ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
			//@formatter:on
	};

	static final Tile BONUS_LOCATION = Tile.at(13, 20);

	protected List<House> houses;
	protected List<Portal> portals;
	protected List<OneWayTile> oneWayTiles;
	protected Bed pacManBed;
	protected ArcadeBonus bonus;

	public ArcadeWorld() {
		super(DATA);
		pacManBed = new Bed(13, 26, Direction.RIGHT);
		//@formatter:off
		houses = List.of(
			HouseBuilder.house()
				.layout(11, 16, 6, 4)
				.door(new Door(Direction.DOWN, 13, 15, 2, 1))
				.bed(13, 14, Direction.LEFT)
				.bed(11, 17, Direction.UP)
				.bed(13, 17, Direction.DOWN)
				.bed(15, 17, Direction.UP)
			.build()
		);
		
		portals = List.of(
			horizontalPortal(Tile.at(1, 17), Tile.at(26, 17))
//		 ,horizontalPortal(Tile.at(1, 11), Tile.at(26, 11))
//		 ,horizontalPortal(Tile.at(1, 23), Tile.at(26, 23))
		);
		
		oneWayTiles = List.of(
			new OneWayTile(12, 13, Direction.DOWN), 
			new OneWayTile(15, 13, Direction.DOWN),
			new OneWayTile(12, 25, Direction.DOWN), 
			new OneWayTile(15, 25, Direction.DOWN)
		);
		//@formatter:on
	}

	@Override
	public Stream<House> houses() {
		return houses.stream();
	}

	@Override
	public House house(int i) {
		return houses.get(i);
	}

	@Override
	public Bed pacManBed() {
		return pacManBed;
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
	public void setFood(Food food, Tile location) {
		int row = location.row, col = location.col;
		if (food.equals(Pellet.SNACK)) {
			map.set1(row, col, B_FOOD);
			map.set0(row, col, B_ENERGIZER);
		} else if (food.equals(Pellet.ENERGIZER)) {
			map.set1(row, col, B_FOOD);
			map.set1(row, col, B_ENERGIZER);
		}
	}

	@Override
	public void addBonusFood(BonusFood bonusFood) {
		if (bonusFood instanceof ArcadeBonus) {
			bonus = (ArcadeBonus) bonusFood;
			bonus.location = BONUS_LOCATION;
		} else {
			throw new IllegalArgumentException("Cannot add this type of bonus food to Aracde world");
		}
	}

	@Override
	public void clearBonusFood() {
		bonus = null;
	}

	@Override
	public Optional<BonusFood> bonusFood() {
		return Optional.ofNullable(bonus);
	}

	/**
	 * There are 4 reserved rows at the top and 3 at the bottom used for displaying scores
	 * 
	 * @return the habitat tiles
	 */
	@Override
	public Stream<Tile> habitat() {
		return IntStream.range(3 * width(), (height() + 4) * width()).mapToObj(i -> Tile.at(i % width(), i / width()));
	}

	@Override
	public Stream<Food> food() {
		return habitat().filter(this::hasFood).map(this::foodAt).map(Optional::get);
	}

	@Override
	public Optional<Food> foodAt(Tile location) {
		if (containsSimplePellet(location)) {
			return Optional.of(Pellet.SNACK);
		}
		if (containsEnergizer(location)) {
			return Optional.of(Pellet.ENERGIZER);
		}
		if (BONUS_LOCATION.equals(location)) {
			return Optional.ofNullable(bonus);
		}
		return Optional.empty();
	}
}