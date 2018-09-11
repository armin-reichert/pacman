package de.amr.games.pacman.actor;

import static java.lang.Math.round;

import de.amr.easy.game.entity.Transform;
import de.amr.easy.game.math.Vector2f;
import de.amr.games.pacman.model.Tile;

/**
 * Mixin for game entities which are placed on tiles.
 * 
 * @author Armin Reichert
 */
public interface TilePlacedEntity {

	int getTileSize();

	Transform tf();

	default int tileCoord(float absoluteCoord) {
		return round(absoluteCoord) / getTileSize();
	}

	default Tile getTile() {
		Vector2f center = tf().getCenter();
		return new Tile(tileCoord(center.x), tileCoord(center.y));
	}

	default void placeAt(Tile tile, float xOffset, float yOffset) {
		tf().setPosition(tile.col * getTileSize() + xOffset, tile.row * getTileSize() + yOffset);
	}

	default void alignOverTile() {
		placeAt(getTile(), 0, 0);
	}

	default boolean isAlignedOverTile() {
		return getTileAlignmentX() == 0 && getTileAlignmentY() == 0;
	}

	default int getTileAlignmentX() {
		return round(tf().getX()) % getTileSize();
	}

	default int getTileAlignmentY() {
		return round(tf().getY()) % getTileSize();
	}
}