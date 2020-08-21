package de.amr.games.pacman.model.world.arcade;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

import de.amr.games.pacman.model.world.api.TemporaryFood;
import de.amr.games.pacman.model.world.api.Direction;
import de.amr.games.pacman.model.world.api.Food;
import de.amr.games.pacman.model.world.components.Bed;
import de.amr.games.pacman.model.world.components.Block;
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

	static final byte[][] MAP = {
			//@formatter:off
			{ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, },
			{ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, },
			{ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, },
			{ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, },
			{ 1, 4, 4, 4, 4, 4, 6, 4, 4, 4, 4, 4, 4, 1, 1, 4, 4, 4, 4, 4, 4, 6, 4, 4, 4, 4, 4, 1, },
			{ 1, 4, 1, 1, 1, 1, 4, 1, 1, 1, 1, 1, 4, 1, 1, 4, 1, 1, 1, 1, 1, 4, 1, 1, 1, 1, 4, 1, },
			{ 1, 4, 1, 1, 1, 1, 4, 1, 1, 1, 1, 1, 4, 1, 1, 4, 1, 1, 1, 1, 1, 4, 1, 1, 1, 1, 4, 1, },
			{ 1, 4, 1, 1, 1, 1, 4, 1, 1, 1, 1, 1, 4, 1, 1, 4, 1, 1, 1, 1, 1, 4, 1, 1, 1, 1, 4, 1, },
			{ 1, 6, 4, 4, 4, 4, 6, 4, 4, 6, 4, 4, 6, 4, 4, 6, 4, 4, 6, 4, 4, 6, 4, 4, 4, 4, 6, 1, },
			{ 1, 4, 1, 1, 1, 1, 4, 1, 1, 4, 1, 1, 1, 1, 1, 1, 1, 1, 4, 1, 1, 4, 1, 1, 1, 1, 4, 1, },
			{ 1, 4, 1, 1, 1, 1, 4, 1, 1, 4, 1, 1, 1, 1, 1, 1, 1, 1, 4, 1, 1, 4, 1, 1, 1, 1, 4, 1, },
			{ 1, 4, 4, 4, 4, 4, 6, 1, 1, 4, 4, 4, 4, 1, 1, 4, 4, 4, 4, 1, 1, 6, 4, 4, 4, 4, 4, 1, },
			{ 1, 1, 1, 1, 1, 1, 4, 1, 1, 1, 1, 1, 0, 1, 1, 0, 1, 1, 1, 1, 1, 4, 1, 1, 1, 1, 1, 1, },
			{ 1, 1, 1, 1, 1, 1, 4, 1, 1, 1, 1, 1, 0, 1, 1, 0, 1, 1, 1, 1, 1, 4, 1, 1, 1, 1, 1, 1, },
			{ 1, 1, 1, 1, 1, 1, 4, 1, 1, 0, 0, 0, 2, 0, 0, 2, 0, 0, 0, 1, 1, 4, 1, 1, 1, 1, 1, 1, },
			{ 1, 1, 1, 1, 1, 1, 4, 1, 1, 0, 1, 1, 1, 0, 0, 1, 1, 1, 0, 1, 1, 4, 1, 1, 1, 1, 1, 1, },
			{ 1, 1, 1, 1, 1, 1, 4, 1, 1, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 1, 1, 4, 1, 1, 1, 1, 1, 1, },
			{ 0, 0, 0, 0, 0, 0, 6, 0, 0, 2, 1, 0, 0, 0, 0, 0, 0, 1, 2, 0, 0, 6, 0, 0, 0, 0, 0, 0, },
			{ 1, 1, 1, 1, 1, 1, 4, 1, 1, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 1, 1, 4, 1, 1, 1, 1, 1, 1, },
			{ 1, 1, 1, 1, 1, 1, 4, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 4, 1, 1, 1, 1, 1, 1, },
			{ 1, 1, 1, 1, 1, 1, 4, 1, 1, 2, 0, 0, 0, 0, 0, 0, 0, 0, 2, 1, 1, 4, 1, 1, 1, 1, 1, 1, },
			{ 1, 1, 1, 1, 1, 1, 4, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 4, 1, 1, 1, 1, 1, 1, },
			{ 1, 1, 1, 1, 1, 1, 4, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 4, 1, 1, 1, 1, 1, 1, },
			{ 1, 4, 4, 4, 4, 4, 6, 4, 4, 6, 4, 4, 4, 1, 1, 4, 4, 4, 6, 4, 4, 6, 4, 4, 4, 4, 4, 1, },
			{ 1, 4, 1, 1, 1, 1, 4, 1, 1, 1, 1, 1, 4, 1, 1, 4, 1, 1, 1, 1, 1, 4, 1, 1, 1, 1, 4, 1, },
			{ 1, 4, 1, 1, 1, 1, 4, 1, 1, 1, 1, 1, 4, 1, 1, 4, 1, 1, 1, 1, 1, 4, 1, 1, 1, 1, 4, 1, },
			{ 1, 4, 4, 4, 1, 1, 6, 4, 4, 6, 4, 4, 6, 0, 0, 6, 4, 4, 6, 4, 4, 6, 1, 1, 4, 4, 4, 1, },
			{ 1, 1, 1, 4, 1, 1, 4, 1, 1, 4, 1, 1, 1, 1, 1, 1, 1, 1, 4, 1, 1, 4, 1, 1, 4, 1, 1, 1, },
			{ 1, 1, 1, 4, 1, 1, 4, 1, 1, 4, 1, 1, 1, 1, 1, 1, 1, 1, 4, 1, 1, 4, 1, 1, 4, 1, 1, 1, },
			{ 1, 4, 4, 6, 4, 4, 4, 1, 1, 4, 4, 4, 4, 1, 1, 4, 4, 4, 4, 1, 1, 4, 4, 4, 6, 4, 4, 1, },
			{ 1, 4, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 4, 1, 1, 4, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 4, 1, },
			{ 1, 4, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 4, 1, 1, 4, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 4, 1, },
			{ 1, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 6, 4, 4, 6, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 1, },
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
	protected Block[] tunnels;
	protected Tile[] energizerTiles;
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
		
		tunnels = new Block[] {
			new Block(1, 17, 5, 1),
			new Block(22, 17, 5, 1),
		};
		
		energizerTiles = new Tile[] {
			Tile.at(1,6),
			Tile.at(26,6),
			Tile.at(1,26),
			Tile.at(26,26),
		};
		//@formatter:on
	}

	@Override
	public int totalFoodCount() {
		return 244;
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
	public void showTemporaryFood(TemporaryFood bonusFood, Tile location) {
		if (!(bonusFood instanceof ArcadeBonus)) {
			throw new IllegalArgumentException("Cannot add this type of bonus food to Arcade world");
		}
		bonus = (ArcadeBonus) bonusFood;
		bonus.setLocation(location);
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