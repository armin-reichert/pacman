package de.amr.games.pacman.actor;

import static java.lang.Math.round;

import de.amr.easy.game.entity.Transform;
import de.amr.easy.game.math.Vector2f;
import de.amr.games.pacman.model.Tile;

/**
 * Mixin with useful functions for game entities which are placed on tiles.
 * 
 * @author Armin Reichert
 */
public interface TilePlacedEntity {

	int getTileSize();

	Transform tf();

	default int tileIndex(float coord) {
		return round(coord) / getTileSize();
	}

	default Tile getTile() {
		Vector2f center = tf().getCenter();
		return new Tile(tileIndex(center.x), tileIndex(center.y));
	}

	default void placeAtTile(Tile tile, float xOffset, float yOffset) {
		tf().setPosition(tile.col * getTileSize() + xOffset, tile.row * getTileSize() + yOffset);
	}

	default void align() {
		placeAtTile(getTile(), 0, 0);
	}

	default boolean isAligned() {
		return getAlignmentX() == 0 && getAlignmentY() == 0;
	}

	default int getAlignmentX() {
		return round(tf().getX()) % getTileSize();
	}

	default int getAlignmentY() {
		return round(tf().getY()) % getTileSize();
	}
}