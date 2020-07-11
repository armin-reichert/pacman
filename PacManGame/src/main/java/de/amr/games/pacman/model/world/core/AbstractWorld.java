package de.amr.games.pacman.model.world.core;

import static de.amr.easy.game.Application.loginfo;
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

import de.amr.games.pacman.controller.api.Creature;
import de.amr.games.pacman.model.world.Direction;
import de.amr.games.pacman.model.world.api.Population;
import de.amr.games.pacman.model.world.api.World;

public abstract class AbstractWorld implements World {

	protected Bed pacManBed;
	protected Tile bonusTile;
	protected List<House> houses = new ArrayList<>();
	protected List<Portal> portals = new ArrayList<>();
	protected List<OneWayTile> oneWayTiles = new ArrayList<>();
	protected Bonus bonus;
	protected boolean changingLevel;
	protected boolean frozen;

	protected WorldMap worldMap;
	protected Population population;

	private Set<Creature> outside = new HashSet<>();

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
	public Population population() {
		return population;
	}

	@Override
	public void bringIn(Creature creature) {
		takeOut(creature, false);
	}

	@Override
	public void takeOut(Creature creature) {
		takeOut(creature, true);
	}

	private void takeOut(Creature creature, boolean out) {
		if (out) {
			outside.add(creature);
			creature.setVisible(false);
			loginfo("%s take out of world", creature.name());
		} else {
			outside.remove(creature);
			creature.setVisible(true);
			loginfo("%s put into world", creature.name());
		}
	}

	@Override
	public boolean contains(Creature creature) {
		return !outside.contains(creature);
	}

	protected void addPortal(Tile left, Tile right) {
		worldMap.set0(left.row, left.col, B_WALL);
		worldMap.set1(left.row, left.col, B_TUNNEL);
		worldMap.set0(right.row, right.col, B_WALL);
		worldMap.set1(right.row, right.col, B_TUNNEL);
		portals.add(new Portal(Tile.at(left.col - 1, left.row), Tile.at(right.col + 1, right.row)));
	}

	protected void setEnergizer(Tile tile) {
		worldMap.set0(tile.row, tile.col, B_WALL);
		worldMap.set1(tile.row, tile.col, B_FOOD);
		worldMap.set1(tile.row, tile.col, B_ENERGIZER);
	}

	protected boolean is(Tile tile, byte bit) {
		return includes(tile) && worldMap.is(tile.row, tile.col, bit);
	}

	protected void set(Tile tile, byte bit) {
		if (includes(tile)) {
			worldMap.set1(tile.row, tile.col, bit);
		}
	}

	protected void clear(Tile tile, byte bit) {
		if (includes(tile)) {
			worldMap.set0(tile.row, tile.col, bit);
		}
	}

	@Override
	public int width() {
		return worldMap.data[0].length;
	}

	@Override
	public int height() {
		return worldMap.data.length;
	}

	@Override
	public int x() {
		return 0;
	}

	@Override
	public int y() {
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
		return inside && !is(tile, B_WALL) || !inside && anyPortalContains(tile);
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
	public Tile neighbor(Tile tile, Direction dir) {
		return tileToDir(tile, dir, 1);
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
	public boolean isJustBeforeDoor(Tile tile) {
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
		return worldMap.totalFoodCount;
	}

	@Override
	public void clearFood() {
		for (int row = 0; row < height(); ++row) {
			for (int col = 0; col < width(); ++col) {
				if (worldMap.is(row, col, B_FOOD)) {
					worldMap.set1(row, col, B_EATEN);
				}
			}
		}
	}

	@Override
	public void fillFood() {
		for (int row = 0; row < height(); ++row) {
			for (int col = 0; col < width(); ++col) {
				if (worldMap.is(row, col, B_FOOD)) {
					worldMap.set0(row, col, B_EATEN);
				}
			}
		}
	}

	@Override
	public void removeFood(Tile tile) {
		if (is(tile, B_FOOD)) {
			set(tile, B_EATEN);
		}
	}

	@Override
	public void createFood(Tile tile) {
		if (is(tile, B_FOOD)) {
			clear(tile, B_EATEN);
		}
	}

	@Override
	public boolean containsFood(Tile tile) {
		return is(tile, B_FOOD) && !is(tile, B_EATEN);
	}

	@Override
	public boolean containsEatenFood(Tile tile) {
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