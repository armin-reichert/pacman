package de.amr.games.pacman.actor;

import static de.amr.games.pacman.model.Game.TS;
import static java.lang.Math.round;

import de.amr.easy.game.entity.Transform;
import de.amr.easy.game.view.View;
import de.amr.games.pacman.model.Tile;

/**
 * An entity with the size of one tile that understands tile coordinates. The sprite (if any) which
 * can be of a different size than one tile is drawn centered over the collision box.
 * 
 * @author Armin Reichert
 */
public interface TileWorldEntity extends View {

	public static Tile getTile(float x, float y) {
		return new Tile(round(x + TS / 2) / TS, round(y + TS / 2) / TS);
	}

	@Override
	default int getWidth() {
		return TS;
	}

	@Override
	default int getHeight() {
		return TS;
	}

	Transform getTransform();

	/**
	 * @return the tile containing the center of the collision box.
	 */
	default Tile getTile() {
		Transform tf = getTransform();
		return getTile(tf.getX(), tf.getY());
	}

	default void placeAtTile(Tile tile, float xOffset, float yOffset) {
		Transform tf = getTransform();
		tf.moveTo(tile.col * TS + xOffset, tile.row * TS + yOffset);
	}

	default void align() {
		placeAtTile(getTile(), 0, 0);
	}

	default boolean isAligned() {
		return getAlignmentX() == 0 && getAlignmentY() == 0;
	}

	default int getAlignmentX() {
		Transform tf = getTransform();
		return round(tf.getX()) % TS;
	}

	default int getAlignmentY() {
		Transform tf = getTransform();
		return round(tf.getY()) % TS;
	}
}