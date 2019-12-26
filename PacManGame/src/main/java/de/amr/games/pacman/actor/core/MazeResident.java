package de.amr.games.pacman.actor.core;

import de.amr.easy.game.view.Lifecycle;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;

/**
 * Implemented by entities that reside in a maze.
 * 
 * @author Armin Reichert
 */
public interface MazeResident extends Lifecycle {

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
	int centerX();

	/**
	 * @return y-coordinate of tile center
	 */
	int centerY();

	/**
	 * Places this maze resident at the given tile, optionally with some offset.
	 * 
	 * @param tile    the tile where this maze mover is placed
	 * @param xOffset pixel offset in x-direction
	 * @param yOffset pixel offset in y-direction
	 */
	void placeAt(Tile tile, byte xOffset, byte yOffset);

	/**
	 * Places this maze resident exactly over the given tile.
	 * 
	 * @param tile the tile where this maze mover is placed
	 */
	default void placeAt(Tile tile) {
		placeAt(tile, (byte) 0, (byte) 0);
	}

	/**
	 * Places this maze resident between the given tile and its right neighbor tile.
	 * 
	 * @param tile the tile where this maze mover is placed
	 */
	default void placeHalfRightOf(Tile tile) {
		placeAt(tile, (byte) (Tile.SIZE / 2), (byte) 0);
	}
}