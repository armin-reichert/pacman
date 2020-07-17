package de.amr.games.pacman.model.world.core;

import static de.amr.games.pacman.model.world.core.WorldMap.B_EATEN;
import static de.amr.games.pacman.model.world.core.WorldMap.B_ENERGIZER;
import static de.amr.games.pacman.model.world.core.WorldMap.B_FOOD;
import static de.amr.games.pacman.model.world.core.WorldMap.B_INTERSECTION;
import static de.amr.games.pacman.model.world.core.WorldMap.B_TUNNEL;
import static de.amr.games.pacman.model.world.core.WorldMap.B_WALL;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import de.amr.games.pacman.model.world.api.Bed;
import de.amr.games.pacman.model.world.api.Bonus;
import de.amr.games.pacman.model.world.api.Direction;
import de.amr.games.pacman.model.world.api.Door;
import de.amr.games.pacman.model.world.api.House;
import de.amr.games.pacman.model.world.api.Lifeform;
import de.amr.games.pacman.model.world.api.OneWayTile;
import de.amr.games.pacman.model.world.api.Portal;
import de.amr.games.pacman.model.world.api.Tile;
import de.amr.games.pacman.model.world.api.World;

public abstract class MapBasedWorld implements World {

	protected WorldMap map;
	protected Bed pacManBed;
	protected List<House> houses = new ArrayList<>();
	protected List<Portal> portals = new ArrayList<>();
	protected List<OneWayTile> oneWayTiles = new ArrayList<>();
	protected Bonus bonus;
	protected Tile bonusTile;
	protected boolean changingLevel;
	protected boolean frozen;

	private Set<Lifeform> excludedGuys = new HashSet<>();

	@Override
	public boolean isFrozen() {
		return frozen;
	}

	@Override
	public void setFrozen(boolean frozen) {
		this.frozen = frozen;
	}

	@Override
	public boolean isChangingLevel() {
		return changingLevel;
	}

	@Override
	public void setChangingLevel(boolean b) {
		changingLevel = b;
	}

	@Override
	public void include(Lifeform creature) {
		exclude(creature, false);
	}

	@Override
	public void exclude(Lifeform creature) {
		exclude(creature, true);
	}

	protected void exclude(Lifeform creature, boolean out) {
		if (out) {
			excludedGuys.add(creature);
		} else {
			excludedGuys.remove(creature);
		}
		creature.setVisible(!out);
	}

	@Override
	public boolean contains(Lifeform creature) {
		return !excludedGuys.contains(creature);
	}

	protected void addPortal(Tile left, Tile right) {
		map.set0(left.row, left.col, B_WALL);
		map.set1(left.row, left.col, B_TUNNEL);
		map.set0(right.row, right.col, B_WALL);
		map.set1(right.row, right.col, B_TUNNEL);
		portals.add(new Portal(Tile.at(left.col - 1, left.row), Tile.at(right.col + 1, right.row)));
	}

	protected void setEnergizer(Tile tile) {
		map.set0(tile.row, tile.col, B_WALL);
		map.set1(tile.row, tile.col, B_FOOD);
		map.set1(tile.row, tile.col, B_ENERGIZER);
	}

	protected boolean is(Tile tile, byte bit) {
		return includes(tile) && map.is(tile.row, tile.col, bit);
	}

	protected void set(Tile tile, byte bit) {
		if (includes(tile)) {
			map.set1(tile.row, tile.col, bit);
		}
	}

	protected void clear(Tile tile, byte bit) {
		if (includes(tile)) {
			map.set0(tile.row, tile.col, bit);
		}
	}

	@Override
	public int width() {
		return map.data[0].length;
	}

	@Override
	public int height() {
		return map.data.length;
	}

	@Override
	public int col() {
		return 0;
	}

	@Override
	public int row() {
		return 0;
	}

	@Override
	public boolean includes(Tile tile) {
		return 0 <= tile.row && tile.row < height() && 0 <= tile.col && tile.col < width();
	}

	@Override
	public Stream<House> houses() {
		return houses.stream();
	}

	@Override
	public Bed pacManBed() {
		return pacManBed;
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
		return isDoor(tile) || houses().map(House::room).anyMatch(room -> room.includes(tile));
	}

	@Override
	public boolean isAccessible(Tile tile) {
		boolean inside = includes(tile);
		return inside && !is(tile, B_WALL) || !inside && isInsidePortal(tile);
	}

	@Override
	public Tile tileToDir(Tile tile, Direction dir, int n) {
		//@formatter:off
		return portals()
			.filter(portal -> portal.includes(tile))
			.findAny()
			.map(portal -> portal.exitTile(tile, dir))
			.orElse(Tile.at(tile.col + n * dir.vector().roundedX(), tile.row + n * dir.vector().roundedY()));
		//@formatter:on
	}

	@Override
	public boolean isIntersection(Tile tile) {
		return is(tile, B_INTERSECTION);
	}

	@Override
	public boolean isDoor(Tile tile) {
		return houses().flatMap(House::doors).anyMatch(door -> door.includes(tile));
	}

	@Override
	public boolean isHouseEntry(Tile tile) {
		for (Direction dir : Direction.values()) {
			Tile neighbor = neighbor(tile, dir);
			if (isDoor(neighbor)) {
				Door door = houses().flatMap(House::doors).filter(d -> d.includes(neighbor)).findFirst().get();
				return door.intoHouse == dir;
			}
		}
		return false;
	}

	@Override
	public boolean isTunnel(Tile tile) {
		return is(tile, B_TUNNEL);
	}

	// food container

	@Override
	public int totalFoodCount() {
		return map.totalFoodCount;
	}

	@Override
	public void clearFood() {
		for (int row = 0; row < height(); ++row) {
			for (int col = 0; col < width(); ++col) {
				if (map.is(row, col, B_FOOD)) {
					map.set1(row, col, B_EATEN);
				}
			}
		}
	}

	@Override
	public void fillFood() {
		for (int row = 0; row < height(); ++row) {
			for (int col = 0; col < width(); ++col) {
				if (map.is(row, col, B_FOOD)) {
					map.set0(row, col, B_EATEN);
				}
			}
		}
	}

	@Override
	public void clearFood(Tile tile) {
		if (is(tile, B_FOOD)) {
			set(tile, B_EATEN);
		}
	}

	@Override
	public void fillFood(Tile tile) {
		if (is(tile, B_FOOD)) {
			clear(tile, B_EATEN);
		}
	}

	@Override
	public boolean containsFood(Tile tile) {
		return is(tile, B_FOOD) && !is(tile, B_EATEN);
	}

	@Override
	public boolean didContainFood(Tile tile) {
		return is(tile, B_FOOD) && is(tile, B_EATEN);
	}

	@Override
	public boolean containsSimplePellet(Tile tile) {
		return containsFood(tile) && !is(tile, B_ENERGIZER);
	}

	@Override
	public boolean containsEnergizer(Tile tile) {
		return containsFood(tile) && is(tile, B_ENERGIZER);
	}

	@Override
	public Optional<Bonus> getBonus() {
		return Optional.ofNullable(bonus);
	}

	@Override
	public void setBonus(Bonus bonus) {
		this.bonus = bonus;
	}
}