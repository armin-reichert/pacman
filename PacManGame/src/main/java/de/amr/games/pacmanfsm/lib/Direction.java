/*
MIT License

Copyright (c) 2019 Armin Reichert

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package de.amr.games.pacmanfsm.lib;

import static de.amr.easy.game.math.V2f.v;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import de.amr.easy.game.math.V2f;

/**
 * Directions in the order UP, RIGHT, DOWN, LEFT.
 * 
 * @author Armin Reichert
 */
public enum Direction {

	UP(0, -1), RIGHT(1, 0), DOWN(0, 1), LEFT(-1, 0);

	private static final Direction[] OPPOSITE_DIR = { DOWN, LEFT, UP, RIGHT };
	private static final Direction[] LEFT_DIR = { LEFT, UP, RIGHT, DOWN };
	private static final Direction[] RIGHT_DIR = { RIGHT, DOWN, LEFT, UP };
	private static final Random RND = new Random();

	public static Stream<Direction> dirs() {
		return Arrays.stream(values());
	}

	public static Stream<Direction> dirsShuffled() {
		List<Direction> dirs = Arrays.asList(Direction.values());
		Collections.shuffle(dirs);
		return dirs.stream();
	}

	public static Direction random() {
		return values()[RND.nextInt(4)];
	}

	private final V2f vector;

	private Direction(int dx, int dy) {
		vector = v(dx, dy);
	}

	public V2f vector() {
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