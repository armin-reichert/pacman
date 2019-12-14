package de.amr.games.pacman.model;

import java.util.Arrays;
import java.util.Iterator;
import java.util.stream.Stream;

/**
 * Data type for directions in the order UP, RIGHT, DOWN, LEFT.
 * 
 * @author Armin Reichert
 */
public enum Direction implements Iterable<Direction> {
	UP(0, -1), RIGHT(1, 0), DOWN(0, 1), LEFT(-1, 0);

	@Override
	public Iterator<Direction> iterator() {
		return Arrays.asList(values()).iterator();
	}

	public static Stream<Direction> dirs() {
		return Arrays.stream(values());
	}

	public Direction opposite() {
		switch (this) {
		case UP:
			return DOWN;
		case DOWN:
			return UP;
		case LEFT:
			return RIGHT;
		case RIGHT:
			return LEFT;
		default:
			throw new IllegalStateException();
		}
	}

	public Direction turnLeft() {
		switch (this) {
		case UP:
			return LEFT;
		case DOWN:
			return RIGHT;
		case LEFT:
			return DOWN;
		case RIGHT:
			return UP;
		default:
			throw new IllegalStateException();
		}
	}

	public Direction turnRight() {
		switch (this) {
		case UP:
			return RIGHT;
		case DOWN:
			return LEFT;
		case LEFT:
			return UP;
		case RIGHT:
			return DOWN;
		default:
			throw new IllegalStateException();
		}
	}

	private Direction(int x, int y) {
		dx = (byte) x;
		dy = (byte) y;
	}

	public final byte dx;
	public final byte dy;
}
