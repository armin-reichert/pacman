package de.amr.games.pacman.model.world.api;

import de.amr.easy.game.controller.Lifecycle;
import de.amr.easy.game.entity.Transform;

/**
 * A lifeform inside its world.
 * 
 * @author Armin Reichert
 */
public interface Life extends Lifecycle {

	/**
	 * @return the transform for this lifeform
	 */
	Transform tf();

	/**
	 * @return x-coordinate of collision box center
	 */
	default float centerX() {
		return tf().getCenter().x;
	}

	/**
	 * @return y-coordinate of collision box center
	 */
	default float centerY() {
		return tf().getCenter().y;
	}

	/**
	 * The tile location is defined as the tile containing the center of the lifeforms body.
	 * 
	 * @return tile location of this lifeform
	 */
	default Tile tileLocation() {
		float cx = centerX(), cy = centerY();
		int col = (int) (cx >= 0 ? cx / Tile.SIZE : Math.floor(cx / Tile.SIZE));
		int row = (int) (cy >= 0 ? cy / Tile.SIZE : Math.floor(cy / Tile.SIZE));
		return Tile.at(col, row);
	}

	/**
	 * The offset between the left side of an entity and the left side of the tile it belongs to.
	 * 
	 * @return the horizontal tile offset
	 */
	default float tileOffsetX() {
		return tf().x - tileLocation().x() + Tile.SIZE / 2;
	}

	/**
	 * The offset between the top side of an entity and the top side of the tile it belongs to.
	 * 
	 * @return the vertical tile offset
	 */
	default float tileOffsetY() {
		return tf().y - tileLocation().y() + Tile.SIZE / 2;
	}

	/**
	 * Euclidean distance (in tiles) between this and the other lifeform.
	 * 
	 * @param other other animal
	 * @return Euclidean distance measured in tiles
	 */
	default double distance(Life other) {
		return tileLocation().distance(other.tileLocation());
	}

	/**
	 * Places this lifeform at the given tile location.
	 * 
	 * @param tile    tile location
	 * @param offsetX offset in x-direction
	 * @param offsetY offset in y-direction
	 */
	default void placeAt(Tile tile, float offsetX, float offsetY) {
		tf().setPosition(tile.x() + offsetX, tile.y() + offsetY);
	}

	World world();

	default boolean isInsideWorld() {
		return world().contains(this);
	}

	boolean isVisible();

	void setVisible(boolean visible);
}