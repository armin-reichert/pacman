package de.amr.games.pacman.actor.core;

import de.amr.easy.game.entity.Entity;
import de.amr.easy.game.math.Vector2f;
import de.amr.games.pacman.model.Tile;

/**
 * An entity residing in a maze. This provides tile coordinates and
 * tile-specific methods for an entity.
 * 
 * @author Armin Reichert
 */
public abstract class AbstractMazeResident extends Entity implements MazeResident {

	private final String name;

	public AbstractMazeResident(String name) {
		this.name = name;
		tf.setWidth(Tile.SIZE);
		tf.setHeight(Tile.SIZE);
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public Tile tile() {
		Vector2f center = tf.getCenter();
		return maze().tileAt(center.roundedX() / Tile.SIZE, center.roundedY() / Tile.SIZE);
	}

	@Override
	public void placeAt(Tile tile, byte xOffset, byte yOffset) {
		tf.setPosition(tile.x() + xOffset, tile.y() + yOffset);
	}
}