package de.amr.games.pacman.model.world.core;

import java.util.Objects;

import de.amr.easy.game.entity.Entity;
import de.amr.easy.game.math.Vector2f;
import de.amr.games.pacman.model.world.api.Direction;
import de.amr.games.pacman.model.world.api.Tile;
import de.amr.games.pacman.model.world.api.World;

/**
 * A lifeform inside its tile-based world.
 * 
 * @author Armin Reichert
 */
public class Lifeform extends Entity {

	public final World world;

	public Lifeform(World world) {
		this.world = world;
	}

	/**
	 * The tile location is defined as the tile containing the center of the lifeforms body.
	 * 
	 * @return tile location of this lifeform
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
	 * Places this lifeform at the given tile location.
	 * 
	 * @param tile tile location
	 * @param dx   additional pixels in x-direction
	 * @param dy   additional pixels in y-direction
	 */
	public void placeAt(Tile tile, float dx, float dy) {
		tf.setPosition(tile.x() + dx, tile.y() + dy);
	}

	/**
	 * Euclidean distance (in tiles) between this and the other lifeform.
	 * 
	 * @param other other animal
	 * @return Euclidean distance measured in tiles
	 */
	public double distance(Lifeform other) {
		return tile().distance(other.tile());
	}

	/**
	 * @return if this lifeform is currently inside its world
	 */
	public boolean isInsideWorld() {
		return world.contains(this);
	}

	/**
	 * The neighbor tile of this lifeforms current tile.
	 * 
	 * @param dir a direction
	 * @return the neighbor tile towards the given direction
	 */
	public Tile neighbor(Direction dir) {
		dir = Objects.requireNonNull(dir);
		return world.tileToDir(tile(), dir, 1);
	}
}