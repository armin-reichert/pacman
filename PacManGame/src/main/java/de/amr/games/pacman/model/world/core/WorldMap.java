package de.amr.games.pacman.model.world.core;

import java.util.Arrays;

/**
 * Map representing a world using bytes.
 * 
 * @author Armin Reichert
 */
public class WorldMap {

	public final byte[][] data;

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
	}

	public int getWidth() {
		return data[0].length;
	}

	public int getHeight() {
		return data.length;
	}
}