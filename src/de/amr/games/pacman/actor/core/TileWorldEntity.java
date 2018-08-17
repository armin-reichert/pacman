package de.amr.games.pacman.actor.core;

import static de.amr.games.pacman.model.Game.TS;
import static java.lang.Math.round;

import java.awt.Graphics2D;

import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.sprite.Sprite;
import de.amr.games.pacman.model.Tile;

/**
 * An entity with the size of one tile that understands tile coordinates. The sprite (if any) which
 * can be of a different size than one tile is drawn centered over the collision box.
 * 
 * @author Armin Reichert
 */
public abstract class TileWorldEntity extends GameEntity {

	@Override
	public int getWidth() {
		return TS;
	}

	@Override
	public int getHeight() {
		return TS;
	}

	/**
	 * @return the tile containing the center of the collision box.
	 */
	public Tile getTile() {
		return new Tile(round(tf.getX() + getWidth() / 2) / TS, round(tf.getY() + getHeight() / 2) / TS);
	}

	public void placeAtTile(Tile tile, float xOffset, float yOffset) {
		tf.moveTo(tile.col * TS + xOffset, tile.row * TS + yOffset);
	}

	public void align() {
		placeAtTile(getTile(), 0, 0);
	}

	public boolean isAligned() {
		return getAlignmentX() == 0 && getAlignmentY() == 0;
	}

	public int getAlignmentX() {
		return round(tf.getX()) % TS;
	}

	public int getAlignmentY() {
		return round(tf.getY()) % TS;
	}

	@Override
	public void draw(Graphics2D g) {
		Sprite sprite = currentSprite();
		if (sprite != null) {
			// center sprite over collision box
			int dx = (getWidth() - sprite.getWidth()) / 2;
			int dy = (getHeight() - sprite.getHeight()) / 2;
			g.translate(dx, dy);
			super.draw(g);
			g.translate(-dx, -dy);
		}
	}
}