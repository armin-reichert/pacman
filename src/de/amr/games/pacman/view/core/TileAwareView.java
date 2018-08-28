package de.amr.games.pacman.view.core;

import static java.lang.Math.round;

import de.amr.easy.game.entity.Transform;
import de.amr.easy.game.view.View;
import de.amr.games.pacman.model.Tile;

/**
 * Mixin for game entities which are aware of a tiled environment.
 * 
 * @author Armin Reichert
 */
public interface TileAwareView extends View {

	int getTileSize();

	Transform getTransform();

	@Override
	default int getWidth() {
		return getTileSize();
	}

	@Override
	default int getHeight() {
		return getTileSize();
	}

	/**
	 * @return the tile containing the center of the entity collision box.
	 */
	default Tile getTile() {
		Transform tf = getTransform();
		return new Tile(round(tf.getX() + getTileSize() / 2) / getTileSize(),
				round(tf.getY() + getTileSize() / 2) / getTileSize());
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