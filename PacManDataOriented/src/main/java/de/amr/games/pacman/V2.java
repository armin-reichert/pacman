package de.amr.games.pacman;

import java.util.Objects;

/**
 * Immutable float 2D vector.
 * 
 * @author Armin Reichert
 */
public class V2 {

	public static final V2 NULL = new V2(0, 0);

	private static float EPS = 0.000001f;

	public static double distance(int x1, int y1, int x2, int y2) {
		return Math.hypot(x1 - x2, y1 - y2);
	}

	public final float x;
	public final float y;

	@Override
	public int hashCode() {
		return Objects.hash(x, y);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		V2 other = (V2) obj;
		return almostEquals(x, other.x) && almostEquals(y, other.y);
	}

	private boolean almostEquals(float x, float y) {
		return x >= y - EPS && x <= y + EPS;
	}

	@Override
	public String toString() {
		return String.format("(%.2f, %.2f)", x, y);
	}

	public V2(float x, float y) {
		this.x = x;
		this.y = y;
	}

	public V2 sum(V2 v) {
		return new V2(x + v.x, y + v.y);
	}

	public V2 scaled(float s) {
		return new V2(s * x, s * y);
	}

	public int x_int() {
		return (int) x;
	}

	public int y_int() {
		return (int) y;
	}
}