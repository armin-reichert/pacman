package de.amr.games.pacman.view.core;

import static java.lang.Math.round;

import de.amr.easy.game.entity.Transform;
import de.amr.games.pacman.model.Tile;

/**
 * Mixin for game entities which are placed on tiles.
 * 
 * @author Armin Reichert
 */
public interface TilePlacedEntity {

	int getTileSize();

	Transform getTransform();

	default int tileCoord(float absoluteCoord) {
		return round(absoluteCoord + getTileSize() / 2) / getTileSize();
	}

	/**
	 * @return the tile containing the center of the entity's collision box.
	 */
	default Tile getTile() {
		return new Tile(tileCoord(getTransform().getX()), tileCoord(getTransform().getY()));
	}

	default void placeAt(Tile tile, float xOffset, float yOffset) {
		getTransform().moveTo(tile.col * getTileSize() + xOffset, tile.row * getTileSize() + yOffset);
	}

	default void align() {
		placeAt(getTile(), 0, 0);
	}

	default boolean isAligned() {
		return getAlignmentX() == 0 && getAlignmentY() == 0;
	}

	default int getAlignmentX() {
		return round(getTransform().getX()) % getTileSize();
	}

	default int getAlignmentY() {
		return round(getTransform().getY()) % getTileSize();
	}
}