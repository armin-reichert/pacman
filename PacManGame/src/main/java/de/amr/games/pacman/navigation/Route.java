package de.amr.games.pacman.navigation;

import java.util.Collections;
import java.util.List;
import java.util.OptionalInt;

import de.amr.easy.grid.impl.Top4;
import de.amr.games.pacman.model.Tile;

/**
 * Represents a route through the maze. For dynamic routes (where the next direction is queried at
 * each intersection), the path may be empty. For fixed routes, the path is computed only once.
 * 
 * @author Armin Reichert
 */
public class Route {

	/**
	 * The direction to take (see class {@link Top4} for the direction constants), {@code -1} means no
	 * value.
	 */
	private int dir;

	/** (Optional) The route as a list of tiles. */
	private List<Tile> path = Collections.emptyList();

	/** The target tile of this route. */
	private Tile target;

	/**
	 * Creates an empty route without defined direction.
	 */
	public Route() {
		this(-1);
	}

	/**
	 * Creates a route following the given direction. The path is empty.
	 * 
	 * @param dir
	 *              route direction
	 */
	public Route(int dir) {
		this.dir = dir;
	}

	/**
	 * @return the direction to take for following this route.
	 */
	public OptionalInt getDir() {
		return dir != -1 ? OptionalInt.of(dir) : OptionalInt.empty();
	}

	/**
	 * Sets the direction for following this route.
	 * 
	 * @param dir
	 *              the direction (Top4.N, Top4.E, Top4.S, Top4.W) or {@code -1}
	 */
	public void setDir(int dir) {
		if (dir == -1 || dir == Top4.N || dir == Top4.E || dir == Top4.S || dir == Top4.W) {
			this.dir = dir;
		} else {
			throw new IllegalArgumentException("Illegal direction value: " + dir);
		}
	}

	/**
	 * @return the path of this route.
	 */
	public List<Tile> getPath() {
		return Collections.unmodifiableList(path);
	}

	/**
	 * Sets the path of this route.
	 * 
	 * @param path
	 *               tile list defining the path
	 */
	public void setPath(List<Tile> path) {
		this.path = path;
	}

	/**
	 * @return the target tile of this route
	 */
	public Tile getTarget() {
		return target;
	}

	/**
	 * Sets the target tile of this route.
	 * 
	 * @param target
	 *                 the target tile
	 */
	public void setTarget(Tile target) {
		this.target = target;
	}
}