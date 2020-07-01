package de.amr.games.pacman.model.world.core;

import java.util.Arrays;

/**
 * Map representing the Pac-Man world.
 * 
 * @author Armin Reichert
 */
public class WorldMap {

	//@formatter:off
	public static final byte B_WALL         = 0;
	public static final byte B_FOOD         = 1;
	public static final byte B_ENERGIZER    = 2;
	public static final byte B_EATEN        = 3;
	public static final byte B_INTERSECTION = 4;
	public static final byte B_TUNNEL       = 5;
	//@formatter:on

	public final byte[][] data;
	public final int totalFoodCount;

	public boolean is(int row, int col, byte bit) {
		return (data[row][col] & (1 << bit)) != 0;
	}

	public void set0(int row, int col, byte bit) {
		data[row][col] &= ~(1 << bit);
	}

	public void set1(int row, int col, byte bit) {
		data[row][col] |= (1 << bit);
	}


	public WorldMap(byte[][] byteArray) {
		data = new byte[byteArray.length][];
		for (int i = 0; i < byteArray.length; ++i) {
			data[i] = Arrays.copyOf(byteArray[i], byteArray[0].length);
		}
		int foodCount = 0;
		for (int row = 0; row < byteArray.length; ++row) {
			for (int col = 0; col < byteArray[0].length; ++col) {
				if (is(row, col, B_FOOD) && !is(row, col, B_EATEN)) {
					++foodCount;
				}
			}
		}
		totalFoodCount = foodCount;
	}
}