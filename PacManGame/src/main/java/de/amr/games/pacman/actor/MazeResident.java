package de.amr.games.pacman.actor;

import static java.lang.Math.round;

import de.amr.easy.game.entity.Entity;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;

/**
 * Implemented by entities residing in a maze.
 * 
 * @author Armin Reichert
 */
public interface MazeResident {

	/**
	 * @return the entity implementing this interface
	 */
	Entity entity();

	/**
	 * @return the maze where this entity resides
	 */
	Maze maze();

	/**
	 * @param other other maze resident
	 * @return squared Euclidean distance to the other maze resident in tile
	 *         coordinates
	 */
	default int tileDistanceSq(MazeResident other) {
		return Tile.distanceSq(tile(), other.tile());
	}

	/**
	 * @return maze column where the center of the collision box is located
	 */
	default int col() {
		return round(entity().tf.getCenter().x) / Maze.TS;
	}

	/**
	 * @return maze row where the center of the collision box is located
	 */
	default int row() {
		return round(entity().tf.getCenter().y) / Maze.TS;
	}

	/**
	 * @return maze tile where the center of the collision box is located
	 */
	default Tile tile() {
		return maze().tileAt(col(), row());
	}

	/**
	 * Places this maze mover at the given tile, optionally with some pixel offset.
	 * 
	 * @param tile    the tile where this maze mover is placed
	 * @param xOffset pixel offset in x-direction
	 * @param yOffset pixel offset in y-direction
	 */
	default void placeAtTile(Tile tile, float xOffset, float yOffset) {
		entity().tf.setPosition(tile.col * Maze.TS + xOffset, tile.row * Maze.TS + yOffset);
	}
}