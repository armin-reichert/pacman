package de.amr.games.pacman.actor.core;

import de.amr.easy.game.entity.Entity;
import de.amr.easy.game.math.Vector2f;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;

/**
 * An entity residing in a maze. This provides tile coordinates and
 * tile-specific methods for an entity.
 * 
 * @author Armin Reichert
 */
public abstract class AbstractMazeResident extends Entity {

	public AbstractMazeResident() {
		tf.setWidth(Tile.SIZE);
		tf.setHeight(Tile.SIZE);
	}

	public abstract Maze maze();

	public Tile tile() {
		Vector2f center = tf.getCenter();
		return maze().tileAt(center.roundedX() / Tile.SIZE, center.roundedY() / Tile.SIZE);
	}

	/**
	 * Places this entity at the given tile with given pixel offsets.
	 * 
	 * @param tile    tile
	 * @param xOffset pixel offset in x-direction
	 * @param yOffset pixel offset in y-direction
	 */
	public void placeAt(Tile tile, byte xOffset, byte yOffset) {
		tf.setPosition(tile.x() + xOffset, tile.y() + yOffset);
	}

	/**
	 * Places this entity exactly over the given tile.
	 * 
	 * @param tile the tile where this maze mover is placed
	 */
	public void placeAt(Tile tile) {
		placeAt(tile, (byte) 0, (byte) 0);
	}

	/**
	 * Places this entity between the given tile and its right neighbor tile.
	 * 
	 * @param tile the tile where this maze mover is placed
	 */
	public void placeHalfRightOf(Tile tile) {
		placeAt(tile, (byte) (Tile.SIZE / 2), (byte) 0);
	}
}