package de.amr.games.pacman.actor.core;

import static java.lang.Math.round;

import de.amr.easy.game.entity.Entity;
import de.amr.easy.game.math.Vector2f;
import de.amr.games.pacman.model.Tile;

/**
 * An entity residing in a maze. This provides tile coordinates and tile-specific methods for an
 * entity.
 * 
 * @author Armin Reichert
 */
public abstract class AbstractMazeResident extends Entity implements MazeResident {

	@Override
	public Tile tile() {
		Vector2f center = tf.getCenter();
		return maze().tileAt(round(center.x) / Tile.SIZE, round(center.y) / Tile.SIZE);
	}

	@Override
	public void placeAtTile(Tile tile, float xOffset, float yOffset) {
		tf.setPosition(tile.col * Tile.SIZE + xOffset, tile.row * Tile.SIZE + yOffset);
	}

	@Override
	public int distanceSq(AbstractMazeResident other) {
		return Tile.distanceSq(tile(), other.tile());
	}
}