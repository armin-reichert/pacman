package de.amr.games.pacman.model.map;

import de.amr.games.pacman.model.world.PacManWorldStructure;

/**
 * Base class for maps representing the Pac-Man world.
 * 
 * @author Armin Reichert
 */
public abstract class PacManMap implements PacManWorldStructure {

	//@formatter:off
	public static final byte B_WALL         = 0;
	public static final byte B_FOOD         = 1;
	public static final byte B_ENERGIZER    = 2;
	public static final byte B_EATEN        = 3;
	public static final byte B_INTERSECTION = 4;
	public static final byte B_TUNNEL       = 5;
	//@formatter:on

	private final byte[][] data;

	public PacManMap(byte[][] data) {
		this.data = data;
	}

	@Override
	public int width() {
		return data[0].length;
	}

	@Override
	public int height() {
		return data.length;
	}
	
	public boolean is(int row, int col, byte bit) {
		return (data[row][col] & (1 << bit)) != 0;
	}

	public void set0(int row, int col, byte bit) {
		data[row][col] &= ~(1 << bit);
	}

	public void set1(int row, int col, byte bit) {
		data[row][col] |= (1 << bit);
	}
}