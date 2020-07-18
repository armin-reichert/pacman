package de.amr.games.pacman.model.world.api;

import de.amr.easy.game.controller.Lifecycle;

/**
 * A lifeform inside its world.
 * 
 * @author Armin Reichert
 */
public interface Lifeform extends Lifecycle {

	Tile tileLocation();

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

	default void placeAt(Tile tile) {
		placeAt(tile, 0, 0);
	}

	World world();

	default boolean isInsideWorld() {
		return world().contains(this);
	}

	boolean isVisible();

	void setVisible(boolean visible);
}