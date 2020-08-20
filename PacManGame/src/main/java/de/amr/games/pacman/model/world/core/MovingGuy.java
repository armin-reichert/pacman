package de.amr.games.pacman.model.world.core;

import de.amr.easy.game.entity.Entity;
import de.amr.games.pacman.model.world.api.Direction;
import de.amr.games.pacman.model.world.components.Tile;

/**
 * An entity that can move through a tile-based world. Guys by default have a body size of exactly
 * one tile but their visual shape normally is larger.
 * 
 * @author Armin Reichert
 */
public class MovingGuy extends Entity {

	public Direction moveDir;
	public Direction wishDir;
	public boolean enteredNewTile;

	public MovingGuy() {
		tf.width = tf.height = Tile.SIZE;
	}

	/**
	 * The tile location is defined as the tile containing the center of the guy's body.
	 * 
	 * @return tile location of this guy
	 */
	public Tile tile() {
		return Tile.at(col(), row());
	}

	/**
	 * The current tile column (x-coordinate).
	 * 
	 * @return column index of current tile
	 */
	public int col() {
		float centerX = tf.x + tf.width / 2;
		return (int) (centerX >= 0 ? centerX / Tile.SIZE : Math.floor(centerX / Tile.SIZE));
	}

	/**
	 * The current tile row (y-coordinate).
	 * 
	 * @return row index of current tile
	 */
	public int row() {
		float centerY = tf.y + tf.height / 2;
		return (int) (centerY >= 0 ? centerY / Tile.SIZE : Math.floor(centerY / Tile.SIZE));
	}

	/**
	 * The deviation from the current tile's x-position from the center of its current tile, a value
	 * between 0 and Tile.SIZE.
	 * 
	 * @return the horizontal tile offset
	 */
	public float tileOffsetX() {
		return (tf.x + tf.width / 2) - col() * Tile.SIZE;
	}

	/**
	 * The deviation from the current tile's y-position from the center of its current tile, a value
	 * between 0 and Tile.SIZE.
	 * 
	 * @return the vertical tile offset
	 */
	public float tileOffsetY() {
		return (tf.y + tf.height / 2) - row() * Tile.SIZE;
	}

	/**
	 * Places this guy at the given tile location.
	 * 
	 * @param tile tile location
	 * @param dx   additional pixels in x-direction
	 * @param dy   additional pixels in y-direction
	 */
	public void placeAt(Tile tile, float dx, float dy) {
		Tile oldTile = tile();
		tf.setPosition(tile.x() + dx, tile.y() + dy);
		enteredNewTile = !tile().equals(oldTile);
	}

	/**
	 * Euclidean distance (in tiles) between this and the other guy.
	 * 
	 * @param other other guy
	 * @return Euclidean distance measured in tiles
	 */
	public double tileDistance(MovingGuy other) {
		return tile().distance(other.tile());
	}
}