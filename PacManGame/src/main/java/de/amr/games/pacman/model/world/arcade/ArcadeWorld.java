package de.amr.games.pacman.model.world.arcade;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import de.amr.games.pacman.model.world.api.BonusFood;
import de.amr.games.pacman.model.world.api.Direction;
import de.amr.games.pacman.model.world.api.Food;
import de.amr.games.pacman.model.world.api.Tile;
import de.amr.games.pacman.model.world.components.Bed;
import de.amr.games.pacman.model.world.components.Door;
import de.amr.games.pacman.model.world.components.House;
import de.amr.games.pacman.model.world.components.OneWayTile;
import de.amr.games.pacman.model.world.components.Portal;
import de.amr.games.pacman.model.world.core.MapBasedWorld;

/**
 * Map-based Pac-Man game world implementation.
 * 
 * @author Armin Reichert
 */
public class ArcadeWorld extends MapBasedWorld {

	public static final byte B_ENERGIZER = 5;

	static final byte[][] DATA = {
			//@formatter:off
			{ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, },
			{ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, },
			{ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, },
			{ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, },
			{ 1, 8, 8, 8, 8, 8,12, 8, 8, 8, 8, 8, 8, 1, 1, 8, 8, 8, 8, 8, 8,12, 8, 8, 8, 8, 8, 1, },
			{ 1, 8, 1, 1, 1, 1, 8, 1, 1, 1, 1, 1, 8, 1, 1, 8, 1, 1, 1, 1, 1, 8, 1, 1, 1, 1, 8, 1, },
			{ 1,40, 1, 1, 1, 1, 8, 1, 1, 1, 1, 1, 8, 1, 1, 8, 1, 1, 1, 1, 1, 8, 1, 1, 1, 1,40, 1, },
			{ 1, 8, 1, 1, 1, 1, 8, 1, 1, 1, 1, 1, 8, 1, 1, 8, 1, 1, 1, 1, 1, 8, 1, 1, 1, 1, 8, 1, },
			{ 1,12, 8, 8, 8, 8,12, 8, 8,12, 8, 8,12, 8, 8,12, 8, 8,12, 8, 8,12, 8, 8, 8, 8,12, 1, },
			{ 1, 8, 1, 1, 1, 1, 8, 1, 1, 8, 1, 1, 1, 1, 1, 1, 1, 1, 8, 1, 1, 8, 1, 1, 1, 1, 8, 1, },
			{ 1, 8, 1, 1, 1, 1, 8, 1, 1, 8, 1, 1, 1, 1, 1, 1, 1, 1, 8, 1, 1, 8, 1, 1, 1, 1, 8, 1, },
			{ 1, 8, 8, 8, 8, 8,12, 1, 1, 8, 8, 8, 8, 1, 1, 8, 8, 8, 8, 1, 1,12, 8, 8, 8, 8, 8, 1, },
			{ 1, 1, 1, 1, 1, 1, 8, 1, 1, 1, 1, 1, 0, 1, 1, 0, 1, 1, 1, 1, 1, 8, 1, 1, 1, 1, 1, 1, },
			{ 1, 1, 1, 1, 1, 1, 8, 1, 1, 1, 1, 1, 0, 1, 1, 0, 1, 1, 1, 1, 1, 8, 1, 1, 1, 1, 1, 1, },
			{ 1, 1, 1, 1, 1, 1, 8, 1, 1, 0, 0, 0, 4, 0, 0, 4, 0, 0, 0, 1, 1, 8, 1, 1, 1, 1, 1, 1, },
			{ 1, 1, 1, 1, 1, 1, 8, 1, 1, 0, 1, 1, 1, 0, 0, 1, 1, 1, 0, 1, 1, 8, 1, 1, 1, 1, 1, 1, },
			{ 1, 1, 1, 1, 1, 1, 8, 1, 1, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 1, 1, 8, 1, 1, 1, 1, 1, 1, },
			{ 2, 2, 2, 2, 2, 2,12, 0, 0, 4, 1, 0, 0, 0, 0, 0, 0, 1, 4, 0, 0,12, 2, 2, 2, 2, 2, 2, },
			{ 1, 1, 1, 1, 1, 1, 8, 1, 1, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 1, 1, 8, 1, 1, 1, 1, 1, 1, },
			{ 1, 1, 1, 1, 1, 1, 8, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 8, 1, 1, 1, 1, 1, 1, },
			{ 1, 1, 1, 1, 1, 1, 8, 1, 1, 4, 0, 0, 0, 0, 0, 0, 0, 0, 4, 1, 1, 8, 1, 1, 1, 1, 1, 1, },
			{ 1, 1, 1, 1, 1, 1, 8, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 8, 1, 1, 1, 1, 1, 1, },
			{ 1, 1, 1, 1, 1, 1, 8, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 8, 1, 1, 1, 1, 1, 1, },
			{ 1, 8, 8, 8, 8, 8,12, 8, 8,12, 8, 8, 8, 1, 1, 8, 8, 8,12, 8, 8,12, 8, 8, 8, 8, 8, 1, },
			{ 1, 8, 1, 1, 1, 1, 8, 1, 1, 1, 1, 1, 8, 1, 1, 8, 1, 1, 1, 1, 1, 8, 1, 1, 1, 1, 8, 1, },
			{ 1, 8, 1, 1, 1, 1, 8, 1, 1, 1, 1, 1, 8, 1, 1, 8, 1, 1, 1, 1, 1, 8, 1, 1, 1, 1, 8, 1, },
			{ 1,40, 8, 8, 1, 1,12, 8, 8,12, 8, 8,12, 0, 0,12, 8, 8,12, 8, 8,12, 1, 1, 8, 8,40, 1, },
			{ 1, 1, 1, 8, 1, 1, 8, 1, 1, 8, 1, 1, 1, 1, 1, 1, 1, 1, 8, 1, 1, 8, 1, 1, 8, 1, 1, 1, },
			{ 1, 1, 1, 8, 1, 1, 8, 1, 1, 8, 1, 1, 1, 1, 1, 1, 1, 1, 8, 1, 1, 8, 1, 1, 8, 1, 1, 1, },
			{ 1, 8, 8,12, 8, 8, 8, 1, 1, 8, 8, 8, 8, 1, 1, 8, 8, 8, 8, 1, 1, 8, 8, 8,12, 8, 8, 1, },
			{ 1, 8, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 8, 1, 1, 8, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 8, 1, },
			{ 1, 8, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 8, 1, 1, 8, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 8, 1, },
			{ 1, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8,12, 8, 8,12, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 1, },
			{ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, },
			{ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, },
			{ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, },
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
			House.construct()
				.layout(11, 16, 6, 4)
				.door(new Door(Direction.DOWN, 13, 15, 2, 1))
				.bed(13, 14, Direction.LEFT)
				.bed(11, 17, Direction.UP)
				.bed(13, 17, Direction.DOWN)
				.bed(15, 17, Direction.UP)
			.build()
		);
		
		portals = List.of(horizontalPortal(Tile.at(0, 17), Tile.at(27, 17)));
		
		oneWayTiles = List.of(
			new OneWayTile(12, 13, Direction.DOWN), 
			new OneWayTile(15, 13, Direction.DOWN),
			new OneWayTile(12, 25, Direction.DOWN), 
			new OneWayTile(15, 25, Direction.DOWN)
		);
		//@formatter:on
	}

	@Override
	public int totalFoodCount() {
		return 244;
	}

	@Override
	public Stream<House> houses() {
		return houses.stream();
	}

	@Override
	public House house(int i) {
		return i < houses.size() ? houses.get(i) : null;
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
	public void addBonusFood(BonusFood bonusFood) {
		if (bonusFood instanceof ArcadeBonus) {
			bonus = (ArcadeBonus) bonusFood;
			bonus.setLocation(BONUS_LOCATION);
		} else {
			throw new IllegalArgumentException("Cannot add this type of bonus food to Arcade world");
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

	@Override
	public Optional<Food> foodAt(Tile location) {
		if (BONUS_LOCATION.equals(location)) {
			return Optional.ofNullable(bonus);
		}
		return hasFood(location) ? is(location, B_ENERGIZER) ? Optional.of(Pellet.ENERGIZER) : Optional.of(Pellet.SNACK)
				: Optional.empty();
	}
}