package de.amr.games.pacman.navigation;

import java.util.Collections;
import java.util.List;

import de.amr.easy.grid.impl.Top4;
import de.amr.games.pacman.model.Tile;

/**
 * Encapsulates the data for taking a route through the maze.
 * 
 * @author Armin Reichert
 */
public class MazeRoute {

	/** The direction to take (see class {@link Top4} for the direction constants. */
	private int dir;

	/** (Optional) The route as a list of tiles. */
	private List<Tile> tiles = Collections.emptyList();

	/** The target tile of this route. */
	private Tile targetTile;

	/**
	 * Creates an empty route without defined direction.
	 */
	public MazeRoute() {
		this(-1);
	}

	/**
	 * Creates a route following the given direction. The path is not defined.
	 * 
	 * @param dir route direction
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
	public List<Tile> getTiles() {
		return Collections.unmodifiableList(tiles);
	}

	/**
	 * Sets the route path as a list of tiles.
	 * 
	 * @param path
	 *               tile list defining the path
	 */
	public void setTiles(List<Tile> path) {
		this.tiles = path;
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