package de.amr.games.pacman.actor.core;

import static java.lang.Math.round;

import java.awt.Graphics2D;

import de.amr.easy.game.entity.GameEntity;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.Tile;

/**
 * Common base class for entities that are located in a tile based world.
 * 
 * @author Armin Reichert
 */
public abstract class TileWorldEntity extends GameEntity {

	@Override
	public int getWidth() {
		return Game.TS;
	}

	@Override
	public int getHeight() {
		return Game.TS;
	}

	public Tile getTile() {
		return new Tile(round(tf.getX() + getWidth() / 2) / Game.TS, round(tf.getY() + getHeight() / 2) / Game.TS);
	}

	public void placeAt(Tile tile) {
		tf.moveTo(tile.col * Game.TS + tile.xOffset, tile.row * Game.TS + tile.yOffset);
	}

	public void placeAt(int col, int row) {
		tf.moveTo(col * Game.TS, row * Game.TS);
	}

	public boolean isGridAligned() {
		return getAlignmentX() == 0 && getAlignmentY() == 0;
	}

	public int getAlignmentX() {
		return round(tf.getX()) % Game.TS;
	}

	public int getAlignmentY() {
		return round(tf.getY()) % Game.TS;
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