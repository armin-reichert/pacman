package de.amr.games.pacman.actor.core;

import static de.amr.games.pacman.model.Game.TS;
import static java.lang.Math.round;

import java.awt.Graphics2D;

import de.amr.easy.game.entity.GameEntity;
import de.amr.games.pacman.model.Tile;

/**
 * Common base class for entities that are located in a tile based world.
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

	public Tile getTile() {
		return new Tile(round(tf.getX() + getWidth() / 2) / TS, round(tf.getY() + getHeight() / 2) / TS);
	}

	public void placeAtTile(Tile tile, float xOffset, float yOffset) {
		tf.moveTo(tile.col * TS + xOffset, tile.row * TS + yOffset);
	}

	public boolean isGridAligned() {
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
		// draw sprite centered over collision box
		int dx = (getWidth() - currentSprite().getWidth()) / 2;
		int dy = (getHeight() - currentSprite().getHeight()) / 2;
		g.translate(dx, dy);
		super.draw(g);
		g.translate(-dx, -dy);
	}
}