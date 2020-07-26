package de.amr.games.pacman.model.world.api;

import java.util.Objects;

import de.amr.easy.game.controller.Lifecycle;
import de.amr.easy.game.entity.Transform;
import de.amr.easy.game.math.Vector2f;

/**
 * A lifeform inside its tile-based world.
 * 
 * @author Armin Reichert
 */
public interface Lifeform extends Lifecycle {

	/**
	 * @return the transform for this lifeform
	 */
	Transform tf();

	/**
	 * The tile location is defined as the tile containing the center of the lifeforms body.
	 * 
	 * @return tile location of this lifeform
	 */
	default Tile tileLocation() {
		Vector2f center = tf().getCenter();
		int col = (int) (center.x >= 0 ? center.x / Tile.SIZE : Math.floor(center.x / Tile.SIZE));
		int row = (int) (center.y >= 0 ? center.y / Tile.SIZE : Math.floor(center.y / Tile.SIZE));
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
	default double distance(Lifeform other) {
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

	/**
	 * @return the world where this lifeform is living
	 */
	World world();

	/**
	 * @return if this lifeform is currently inside its world
	 */
	default boolean isInsideWorld() {
		return world().contains(this);
	}

	/**
	 * The neighbor tile of this lifeforms current tile.
	 * 
	 * @param dir a direction
	 * @return the neighbor tile towards the given direction
	 */
	default Tile neighbor(Direction dir) {
		dir = Objects.requireNonNull(dir);
		return world().tileToDir(tileLocation(), dir, 1);
	}

	/**
	 * @return if this lifeform is visible
	 */
	boolean isVisible();

	/**
	 * Makes this lifeform visible or invisible.
	 * 
	 * @param visible if visible or not
	 */
	void setVisible(boolean visible);
}