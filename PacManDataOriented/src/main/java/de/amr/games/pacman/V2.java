package de.amr.games.pacman;

import java.util.Objects;

public class V2 {

	public static final V2 NULL = new V2(0, 0);
	public static final V2 RIGHT = new V2(1, 0);
	public static final V2 LEFT = new V2(-1, 0);
	public static final V2 UP = new V2(0, -1);
	public static final V2 DOWN = new V2(0, 1);

	public float x;
	public float y;

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
		return Float.floatToIntBits(x) == Float.floatToIntBits(other.x)
				&& Float.floatToIntBits(y) == Float.floatToIntBits(other.y);
	}

	@Override
	public String toString() {
		return String.format("(%.2f, %.2f)", x, y);
	}

	public V2(float x, float y) {
		this.x = x;
		this.y = y;
	}

	public V2 copy() {
		return new V2(x, y);
	}

	public void add(V2 v) {
		x += v.x;
		y += v.y;
	}

	public void scale(float s) {
		x *= s;
		y *= s;
	}

	public V2 sum(V2 v) {
		V2 sum = copy();
		sum.add(v);
		return sum;
	}

	public V2 scaled(float s) {
		V2 scaled = copy();
		scaled.scale(s);
		return scaled;
	}
}