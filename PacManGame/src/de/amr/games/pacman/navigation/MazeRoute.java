package de.amr.games.pacman.navigation;

import java.util.Collections;
import java.util.List;

import de.amr.easy.grid.impl.Top4;
import de.amr.games.pacman.model.Tile;

/**
 * Represents a route through the maze. For dynamic routes (where the direction to be taken is
 * queried at each crossing), the path may be empty. For fixed routes it is computed only once.
 * 
 * @author Armin Reichert
 */
public class MazeRoute {

	/** The direction to take (see class {@link Top4} for the direction constants. */
	private int dir;

	/** (Optional) The route as a list of tiles. */
	private List<Tile> path = Collections.emptyList();

	/** The target tile of this route. */
	private Tile targetTile;

	/**
	 * Creates an empty route without defined direction.
	 */
	public MazeRoute() {
		this(-1);
	}

	/**
	 * Creates a route following the given direction. The path is empty.
	 * 
	 * @param dir
	 *              route direction
	 */
	public MazeRoute(int dir) {
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
	 * @return the (optional) list of tiles defining this route.
	 */
	public List<Tile> getPath() {
		return Collections.unmodifiableList(path);
	}

	/**
	 * Sets the path (as a list of tiles) for this route.
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
	public Tile getTargetTile() {
		return targetTile;
	}

	/**
	 * Sets the target tile of this route.
	 * 
	 * @param targetTile
	 *                     the target tile
	 */
	public void setTargetTile(Tile targetTile) {
		this.targetTile = targetTile;
	}
}