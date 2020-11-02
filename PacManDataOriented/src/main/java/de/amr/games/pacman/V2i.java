package de.amr.games.pacman;

import java.util.Objects;

/**
 * Immutable int 2D vector.
 * 
 * @author Armin Reichert
 */
public class V2i {

	public final int x;
	public final int y;

	public V2i(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public V2i scaled(int s) {
		return new V2i(s * x, s * y);
	}

	public V2i sum(V2i v) {
		return new V2i(x + v.x, y + v.y);
	}

	public double distance(V2i v) {
		return Math.hypot(x - v.x, y - v.y);
	}

	@Override
	public int hashCode() {
		return Objects.hash(x, y);
	}

	@Override
	public String toString() {
		return String.format("V2i(%2d,%2d)", x, y);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		V2i other = (V2i) obj;
		return x == other.x && y == other.y;
	}
}