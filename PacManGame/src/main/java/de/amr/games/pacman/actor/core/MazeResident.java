package de.amr.games.pacman.actor.core;

import de.amr.easy.game.view.Controller;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;

/**
 * Implemented by entities that reside in a maze.
 * 
 * @author Armin Reichert
 */
public interface MazeResident extends Controller {
	
	/**
	 * @return descriptive name
	 */
	String name();

	/**
	 * Make me visible.
	 */
	void show();

	/**
	 * Makes me invisible.
	 */
	void hide();

	/**
	 * @return my maze
	 */
	Maze maze();

	/**
	 * @return maze tile where the center of the collision box is located
	 */
	Tile tile();

	/**
	 * @return x-coordinate of tile center
	 */
	default int centerX() {
		return tile().centerX();
	}

	/**
	 * @return y-coordinate of tile center
	 */
	default int centerY() {
		return tile().centerY();
	}

	/**
	 * Places this maze resident at the given tile, optionally with some offset.
	 * 
	 * @param tile    the tile where this maze mover is placed
	 * @param xOffset pixel offset in x-direction
	 * @param yOffset pixel offset in y-direction
	 */
	void placeAtTile(Tile tile, float xOffset, float yOffset);

	/**
	 * @param other other maze resident
	 * @return squared Euclidean distance to the other maze resident in tile
	 *         coordinates
	 */
	int distanceSq(AbstractMazeResident other);

}
