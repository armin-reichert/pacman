package de.amr.games.pacman.model.world.core;

import java.util.Arrays;

/**
 * 2D-map of bytes.
 * 
 * @author Armin Reichert
 */
public class ByteMap {

	private final byte[][] data;

	/**
	 * @param row      a row
	 * @param col      a column
	 * @param bitIndex a bit index (0..7)
	 * @return the boolean value of the bit at the given position and index
	 */
	public boolean is(int row, int col, short bitIndex) {
		return (data[row][col] & (1 << bitIndex)) != 0;
	}

	/**
	 * Clears the bit at the given map position and index
	 * 
	 * @param row      a row
	 * @param col      a column
	 * @param bitIndex a bit index (0..7)
	 */
	public void set0(int row, int col, short bitIndex) {
		data[row][col] &= ~(1 << bitIndex);
	}

	/**
	 * Sets the bit at the given map position and index
	 * 
	 * @param row      a row
	 * @param col      a column
	 * @param bitIndex a bit index (0..7)
	 */
	public void set1(int row, int col, short bitIndex) {
		data[row][col] |= (1 << bitIndex);
	}

	/**
	 * Creates a new map using a copy of the given byte array. The rows of the byte array must all have
	 * the same length.
	 * 
	 * @param byteArray byte array
	 */
	public ByteMap(byte[][] byteArray) {
		data = new byte[byteArray.length][];
		for (int i = 0; i < byteArray.length; ++i) {
			data[i] = Arrays.copyOf(byteArray[i], byteArray[0].length);
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