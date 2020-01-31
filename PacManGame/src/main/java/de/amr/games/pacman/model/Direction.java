package de.amr.games.pacman.model;

import java.util.Arrays;
import java.util.Iterator;
import java.util.stream.Stream;

import de.amr.easy.game.math.Vector2f;

/**
 * Data type for directions in the order UP, RIGHT, DOWN, LEFT.
 * 
 * @author Armin Reichert
 */
public enum Direction implements Iterable<Direction> {
	UP, RIGHT, DOWN, LEFT;

	static Vector2f[] VECTOR = { Vector2f.of(0, -1), Vector2f.of(1, 0), Vector2f.of(0, 1), Vector2f.of(-1, 0) };
	static Direction[] OPPOSITE = { DOWN, LEFT, UP, RIGHT };
	static Direction[] LEFT_OF = { LEFT, UP, RIGHT, DOWN };
	static Direction[] RIGHT_OF = { RIGHT, DOWN, LEFT, UP };

	@Override
	public Iterator<Direction> iterator() {
		return Arrays.asList(values()).iterator();
	}

	public static Stream<Direction> dirs() {
		return Arrays.stream(values());
	}

	public Vector2f vector() {
		return VECTOR[ordinal()];
	}

	public Direction opposite() {
		return OPPOSITE[ordinal()];
	}

	public Direction turnLeft() {
		return LEFT_OF[ordinal()];
	}

	public Direction turnRight() {
		return RIGHT_OF[ordinal()];
	}
}