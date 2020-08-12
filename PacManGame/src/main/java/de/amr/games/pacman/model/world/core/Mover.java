package de.amr.games.pacman.model.world.core;

import de.amr.easy.game.entity.Entity;
import de.amr.easy.game.math.Vector2f;
import de.amr.games.pacman.model.world.api.Direction;
import de.amr.games.pacman.model.world.api.Tile;

/**
 * An entity that can move through a tile-based world.
 * 
 * @author Armin Reichert
 */
public class Mover extends Entity {

	public Direction moveDir;
	public Direction wishDir;
	public boolean enteredNewTile;

	/**
	 * The tile location is defined as the tile containing the center of the lifeforms body.
	 * 
	 * @return tile location of this mover
	 */
	public Tile tile() {
		Vector2f center = tf.getCenter();
		int col = (int) (center.x >= 0 ? center.x / Tile.SIZE : Math.floor(center.x / Tile.SIZE));
		int row = (int) (center.y >= 0 ? center.y / Tile.SIZE : Math.floor(center.y / Tile.SIZE));
		return Tile.at(col, row);
	}

	/**
	 * The offset between the left side of an entity and the left side of the tile it belongs to.
	 * 
	 * @return the horizontal tile offset
	 */
	public float tileOffsetX() {
		return tf.x - tile().x() + Tile.SIZE / 2;
	}

	/**
	 * The offset between the top side of an entity and the top side of the tile it belongs to.
	 * 
	 * @return the vertical tile offset
	 */
	public float tileOffsetY() {
		return tf.y - tile().y() + Tile.SIZE / 2;
	}

	/**
	 * Places this mover at the given tile location.
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
	 * Euclidean distance (in tiles) between this and the other mover.
	 * 
	 * @param other other animal
	 * @return Euclidean distance measured in tiles
	 */
	public double tileDistance(Mover other) {
		return tile().distance(other.tile());
	}
}