package de.amr.games.pacman.model.world.core;

import java.util.Arrays;

/**
 * 2D-map of bytes.
 * 
 * @author Armin Reichert
 */
public class ByteMap {

	private final byte[][] data;

	private static void rangeCheck(int index) {
		if (index < 0 || index > 7) {
			throw new IllegalArgumentException("Bit index out of range [0..7]: " + index);
		}
	}

	/**
	 * @param row      a row
	 * @param col      a column
	 * @param bitIndex a bit index (0..7)
	 * @return the boolean value of the bit at the given position and index
	 */
	public boolean is(int row, int col, int bitIndex) {
		rangeCheck(bitIndex);
		return (data[row][col] & (1 << bitIndex)) != 0;
	}

	/**
	 * Clears the bit at the given map position and index
	 * 
	 * @param row      a row
	 * @param col      a column
	 * @param bitIndex a bit index (0..7)
	 */
	public void set0(int row, int col, int bitIndex) {
		rangeCheck(bitIndex);
		data[row][col] &= ~(1 << bitIndex);
	}

	/**
	 * Sets the bit at the given map position and index
	 * 
	 * @param row      a row
	 * @param col      a column
	 * @param bitIndex a bit index (0..7)
	 */
	public void set1(int row, int col, int bitIndex) {
		rangeCheck(bitIndex);
		data[row][col] |= (1 << bitIndex);
	}

	/**
	 * Creates a new map using a copy of the given byte array. The rows of the byte array must all have
	 * the same length.
	 * 
	 * @param array2D two-dimensional byte array
	 */
	public ByteMap(byte[][] array2D) {
		data = new byte[array2D.length][];
		int width = array2D[0].length;
		for (int i = 0; i < array2D.length; ++i) {
			byte[] row = array2D[i];
			if (row.length != width) {
				throw new IllegalStateException("Rows must all have the same length");
			}
			data[i] = Arrays.copyOf(row, width);
		}
	}

	/**
	 * @return the number of columns of the map
	 */
	public int getWidth() {
		return data[0].length;
	}

	/**
	 * @return the number of rows of the map
	 */
	public int getHeight() {
		return data.length;
	}
}