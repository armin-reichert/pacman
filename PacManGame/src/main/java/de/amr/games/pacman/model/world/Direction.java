package de.amr.games.pacman.model.world;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import de.amr.easy.game.math.Vector2f;

/**
 * Directions in the order UP, RIGHT, DOWN, LEFT.
 * 
 * @author Armin Reichert
 */
public enum Direction {
	UP(0, -1), RIGHT(1, 0), DOWN(0, 1), LEFT(-1, 0);

	static Direction[] OPPOSITE_DIR = { DOWN, LEFT, UP, RIGHT };
	static Direction[] LEFT_DIR = { LEFT, UP, RIGHT, DOWN };
	static Direction[] RIGHT_DIR = { RIGHT, DOWN, LEFT, UP };

	public static Stream<Direction> dirs() {
		return Arrays.stream(values());
	}

	public static Stream<Direction> dirsShuffled() {
		List<Direction> dirs = Arrays.asList(Direction.values());
		Collections.shuffle(dirs);
		return dirs.stream();
	}

	private final Vector2f vector;

	private Direction(int dx, int dy) {
		vector = Vector2f.of(dx, dy);
	}

	public Vector2f vector() {
		return vector;
	}

	public Direction opposite() {
		return OPPOSITE_DIR[ordinal()];
	}

	public Direction left() {
		return LEFT_DIR[ordinal()];
	}

	public Direction right() {
		return RIGHT_DIR[ordinal()];
	}

}