package de.amr.games.pacman.model.world.arcade;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

import de.amr.games.pacman.model.world.api.BonusFood;
import de.amr.games.pacman.model.world.api.Direction;
import de.amr.games.pacman.model.world.api.Food;
import de.amr.games.pacman.model.world.components.Bed;
import de.amr.games.pacman.model.world.components.Door;
import de.amr.games.pacman.model.world.components.House;
import de.amr.games.pacman.model.world.components.OneWayTile;
import de.amr.games.pacman.model.world.components.Portal;
import de.amr.games.pacman.model.world.components.Tile;
import de.amr.games.pacman.model.world.core.MapBasedWorld;

/**
 * The world of the Arcade version of the game.
 * 
 * @author Armin Reichert
 */
public class ArcadeWorld extends MapBasedWorld {

	public static final byte B_ENERGIZER = 5;

	static final byte[][] MAP = {
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

	public static final Tile BONUS_LOCATION = Tile.at(13, 20);

	protected House house;
	protected Bed pacManBed;
	protected Portal portal;
	protected OneWayTile[] oneWayTiles;
	protected ArcadeBonus bonus;

	public ArcadeWorld() {
		super(MAP);
		portal = horizontalPortal(Tile.at(0, 17), Tile.at(27, 17));
		pacManBed = new Bed(13, 26, Direction.RIGHT);
		//@formatter:off
		house =	House
			.world(this)
			.layout(10, 15, 8, 5)
			.door(new Door(Direction.DOWN, 13, 15, 2, 1))
			.bed(new Bed(13, 14, Direction.LEFT))
			.bed(new Bed(11, 17, Direction.UP))
			.bed(new Bed(13, 17, Direction.DOWN))
			.bed(new Bed(15, 17, Direction.UP))
			.build();
		
		oneWayTiles = new OneWayTile[] {
			new OneWayTile(12, 13, Direction.DOWN), 
			new OneWayTile(15, 13, Direction.DOWN),
			new OneWayTile(12, 25, Direction.DOWN), 
			new OneWayTile(15, 25, Direction.DOWN)
		};
		//@formatter:on
	}

	@Override
	public int totalFoodCount() {
		return 244;
	}

	@Override
	public Stream<House> houses() {
		return Stream.of(house);
	}

	@Override
	public House house(int i) {
		return i == 0 ? house : null;
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
	public void showBonusFood(BonusFood bonusFood, Tile location) {
		if (!(bonusFood instanceof ArcadeBonus)) {
			throw new IllegalArgumentException("Cannot add this type of bonus food to Arcade world");
		}
		bonus = (ArcadeBonus) bonusFood;
		bonus.setLocation(location);
		bonus.show();
	}

	@Override
	public void hideBonusFood() {
		bonus = null;
	}

	@Override
	public Optional<BonusFood> bonusFood() {
		return Optional.ofNullable(bonus);
	}

	@Override
	public Optional<Food> foodAt(Tile location) {
		if (bonus != null && bonus.location().equals(location)) {
			return Optional.of(bonus);
		}
		return hasFood(location)
				? is(location, B_ENERGIZER) ? Optional.of(ArcadeFood.ENERGIZER) : Optional.of(ArcadeFood.PELLET)
				: Optional.empty();
	}
}