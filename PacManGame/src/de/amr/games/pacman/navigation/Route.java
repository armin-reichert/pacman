package de.amr.games.pacman.navigation;

import java.util.Collections;
import java.util.List;

import de.amr.easy.grid.impl.Top4;
import de.amr.games.pacman.model.Tile;

/**
 * Represents a route through the maze. For dynamic routes (where the next direction is queried at
 * each intersection), the path may be empty. For fixed routes, the path is computed only once.
 * 
 * @author Armin Reichert
 */
public class Route {

	/** The direction to take (see class {@link Top4} for the direction constants. */
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
	 * @return the next direction to take for following this route or {@code -1} if no information is
	 *         available.
	 */
	public int getDir() {
		return dir;
	}

	/**
	 * Sets the next direction for this route.
	 * 
	 * @param dir
	 */
	public void setDir(int dir) {
		this.dir = dir;
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