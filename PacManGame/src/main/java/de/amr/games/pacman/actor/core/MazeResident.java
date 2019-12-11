package de.amr.games.pacman.actor.core;

import static java.lang.Math.round;

import de.amr.easy.game.entity.Entity;
import de.amr.easy.game.math.Vector2f;
import de.amr.easy.game.view.Controller;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;

/**
 * Implemented by entities residing in a maze. This provides tile coordinates
 * and tile-specific methods for an entity.
 * 
 * @author Armin Reichert
 */
public interface MazeResident extends Controller {

	/**
	 * @return the entity implementing this interface
	 */
	Entity resident();

	/**
	 * @return the maze where this entity resides
	 */
	Maze maze();

	/**
	 * @return maze tile where the center of the collision box is located
	 */
	default Tile tile() {
		Vector2f center = resident().tf.getCenter();
		return maze().tileAt(round(center.x) / Maze.TS, round(center.y) / Maze.TS);
	}

	/**
	 * Places this maze resident at the given tile, optionally with some offset.
	 * 
	 * @param tile    the tile where this maze mover is placed
	 * @param xOffset pixel offset in x-direction
	 * @param yOffset pixel offset in y-direction
	 */
	default void placeAtTile(Tile tile, float xOffset, float yOffset) {
		resident().tf.setPosition(tile.col * Maze.TS + xOffset, tile.row * Maze.TS + yOffset);
	}

	/**
	 * @param other other maze resident
	 * @return squared Euclidean distance to the other maze resident in tile
	 *         coordinates
	 */
	default int distanceSq(MazeResident other) {
		return Tile.distanceSq(tile(), other.tile());
	}
}