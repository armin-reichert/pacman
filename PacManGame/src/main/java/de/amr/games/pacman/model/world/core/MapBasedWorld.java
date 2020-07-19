package de.amr.games.pacman.model.world.core;

import static de.amr.games.pacman.model.world.core.WorldMap.B_EATEN;
import static de.amr.games.pacman.model.world.core.WorldMap.B_ENERGIZER;
import static de.amr.games.pacman.model.world.core.WorldMap.B_FOOD;
import static de.amr.games.pacman.model.world.core.WorldMap.B_INTERSECTION;
import static de.amr.games.pacman.model.world.core.WorldMap.B_TUNNEL;
import static de.amr.games.pacman.model.world.core.WorldMap.B_WALL;

import de.amr.games.pacman.model.world.api.Direction;
import de.amr.games.pacman.model.world.api.Portal;
import de.amr.games.pacman.model.world.api.Tile;

/**
 * Base class for worlds using a map.
 * 
 * @author Armin Reichert
 */
public abstract class MapBasedWorld extends AbstractWorld {

	protected final WorldMap map;

	public MapBasedWorld(byte[][] data) {
		map = new WorldMap(data);
	}

	protected Portal horizontalPortal(Tile leftEntry, Tile rightEntry) {
		Tile left = Tile.at(leftEntry.col - 1, leftEntry.row);
		Tile right = Tile.at(rightEntry.col + 1, rightEntry.row);
		map.set0(left.row, left.col, B_WALL);
		map.set1(left.row, left.col, B_TUNNEL);
		map.set0(right.row, right.col, B_WALL);
		map.set1(right.row, right.col, B_TUNNEL);
		return new Portal(left, right);
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
}