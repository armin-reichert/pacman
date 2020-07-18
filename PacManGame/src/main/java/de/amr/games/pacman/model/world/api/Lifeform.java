package de.amr.games.pacman.model.world.api;

import de.amr.easy.game.controller.Lifecycle;

/**
 * A lifeform inside its world.
 * 
 * @author Armin Reichert
 */
public interface Lifeform extends Lifecycle {

	float centerX();

	float centerY();

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

	float tileOffsetX();

	float tileOffsetY();

	/**
	 * Euclidean distance (in tiles) between this and the other lifeform.
	 * 
	 * @param other other animal
	 * @return Euclidean distance measured in tiles
	 */
	default double distance(Lifeform other) {
		return tileLocation().distance(other.tileLocation());
	}

	void placeAt(Tile tile, float offsetX, float offsetY);

	World world();

	default boolean isInsideWorld() {
		return world().contains(this);
	}

	boolean isVisible();

	void setVisible(boolean visible);
}