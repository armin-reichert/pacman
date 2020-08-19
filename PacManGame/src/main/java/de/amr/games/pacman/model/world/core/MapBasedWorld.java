package de.amr.games.pacman.model.world.core;

import de.amr.games.pacman.model.world.api.Tile;
import de.amr.games.pacman.model.world.components.Portal;

/**
 * Base class for worlds using a map.
 * 
 * @author Armin Reichert
 */
public abstract class MapBasedWorld extends AbstractWorld {

	//@formatter:off
	public static final byte B_WALL         = 0;
	public static final byte B_TUNNEL       = 1;
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

	protected Portal horizontalPortal(Tile left, Tile right) {
		map.set0(left.row, left.col, B_WALL);
		map.set1(left.row, left.col, B_TUNNEL);
		map.set0(right.row, right.col, B_WALL);
		map.set1(right.row, right.col, B_TUNNEL);
		return new Portal(left, right, false);
	}

	protected Portal verticalPortal(Tile top, Tile bottom) {
		map.set0(top.row, top.col, B_WALL);
		map.set1(top.row, top.col, B_TUNNEL);
		map.set0(bottom.row, bottom.col, B_WALL);
		map.set1(bottom.row, bottom.col, B_TUNNEL);
		return new Portal(top, bottom, true);
	}

	protected boolean is(Tile tile, int bit) {
		return includes(tile) && map.is(tile.row, tile.col, bit);
	}

	protected void set(Tile tile, int bit) {
		if (includes(tile)) {
			map.set1(tile.row, tile.col, bit);
		}
	}

	protected void clear(Tile tile, int bit) {
		if (includes(tile)) {
			map.set0(tile.row, tile.col, bit);
		}
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
		boolean inside = includes(tile);
		return inside && !is(tile, B_WALL) || isPortal(tile);
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
	public boolean hasFood(Tile tile) {
		return is(tile, B_FOOD) && !is(tile, B_EATEN);
	}

	@Override
	public boolean hasEatenFood(Tile tile) {
		return is(tile, B_FOOD) && is(tile, B_EATEN);
	}
}