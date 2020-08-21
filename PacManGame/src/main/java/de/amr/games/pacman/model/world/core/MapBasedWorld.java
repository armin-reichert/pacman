package de.amr.games.pacman.model.world.core;

import de.amr.games.pacman.model.world.components.Portal;
import de.amr.games.pacman.model.world.components.Tile;

/**
 * Base class for worlds using a map.
 * 
 * @author Armin Reichert
 */
public abstract class MapBasedWorld extends AbstractWorld {

	//@formatter:off
	public static final byte B_WALL         = 0;
	public static final byte B_1            = 1;
	public static final byte B_INTERSECTION = 2;
	public static final byte B_FOOD         = 3;
	public static final byte B_EATEN        = 4;
	//@formatter:on

	protected final ByteMap map;

	public MapBasedWorld(byte[][] data) {
		super(data.length, data[0].length);
		map = new ByteMap(data);
	}

	public ByteMap map() {
		return map;
	}

	protected Portal horizontalPortal(Tile either, Tile other) {
		map.set0(either.row, either.col, B_WALL);
		map.set0(other.row, other.col, B_WALL);
		return new Portal(either, other, false);
	}

	protected Portal verticalPortal(Tile either, Tile other) {
		map.set0(either.row, either.col, B_WALL);
		map.set0(other.row, other.col, B_WALL);
		return new Portal(either, other, true);
	}

	protected boolean is(Tile tile, int bit) {
		return includes(tile) && map.is(tile.row, tile.col, bit);
	}

	@Override
	public int width() {
		return map.getWidth();
	}

	@Override
	public int height() {
		return map.getHeight();
	}

	@Override
	public boolean isAccessible(Tile tile) {
		return includes(tile) && !is(tile, B_WALL);
	}

	@Override
	public boolean isIntersection(Tile tile) {
		return is(tile, B_INTERSECTION);
	}

	// food container

	@Override
	public void restoreFood() {
		for (int row = 0; row < height(); ++row) {
			for (int col = 0; col < width(); ++col) {
				if (map.is(row, col, B_FOOD)) {
					map.set0(row, col, B_EATEN);
				}
			}
		}
	}

	@Override
	public void eatFood(Tile tile) {
		if (is(tile, B_FOOD)) {
			map.set1(tile.row, tile.col, B_EATEN);
		}
	}

	@Override
	public boolean hasFood(Tile tile) {
		return is(tile, B_FOOD) && !is(tile, B_EATEN);
	}

	@Override
	public boolean hasEatenFood(Tile tile) {
		return is(tile, B_FOOD) && is(tile, B_EATEN);
	}
}