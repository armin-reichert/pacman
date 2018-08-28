package de.amr.games.pacman.view.core;

import static java.lang.Math.round;

import de.amr.easy.game.entity.Transform;
import de.amr.games.pacman.model.Tile;

/**
 * Mixin for game entities which are aware of a tiled environment.
 * 
 * @author Armin Reichert
 */
public interface TilePlacedEntity {

	int getTileSize();

	Transform getTransform();

	default int tileCoord(float f) {
		return round(f + getTileSize() / 2) / getTileSize();
	}

	/**
	 * @return the tile containing the center of the entity's collision box.
	 */
	default Tile getTile() {
		Transform tf = getTransform();
		return new Tile(tileCoord(tf.getX()), tileCoord(tf.getY()));
	}

	default void placeAtTile(Tile tile, float xOffset, float yOffset) {
		Transform tf = getTransform();
		tf.moveTo(tile.col * getTileSize() + xOffset, tile.row * getTileSize() + yOffset);
	}

	default void align() {
		placeAtTile(getTile(), 0, 0);
	}

	default boolean isAligned() {
		return getAlignmentX() == 0 && getAlignmentY() == 0;
	}

	default int getAlignmentX() {
		Transform tf = getTransform();
		return round(tf.getX()) % getTileSize();
	}

	default int getAlignmentY() {
		Transform tf = getTransform();
		return round(tf.getY()) % getTileSize();
	}
}