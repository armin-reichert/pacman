package de.amr.games.pacman.actor;

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

	public int row() {
		return round(tf.getY() + getHeight() / 2) / Game.TS;
	}

	public int col() {
		return round(tf.getX() + getWidth() / 2) / Game.TS;
	}

	public Tile getTile() {
		return new Tile(col(), row());
	}

	public void placeAt(Tile tile) {
		placeAt(tile.col, tile.row);
	}

	public void placeAt(Tile tile, float offsetx, float offsety) {
		placeAt(tile.col, tile.row);
	}
	
	public void placeAt(float col, float row) {
		tf.moveTo(col * Game.TS, row * Game.TS);
	}

	public boolean isExactlyOverTile() {
		return round(tf.getX()) % Game.TS == 0 && round(tf.getY()) % Game.TS == 0;
	}

	@Override
	public void draw(Graphics2D g) {
		// by default, draw sprite centered over collision box
		int dx = (getWidth() - currentSprite().getWidth()) / 2;
		int dy = (getHeight() - currentSprite().getHeight()) / 2;
		g.translate(dx, dy);
		super.draw(g);
		g.translate(-dx, -dy);
	}

	@Override
	public int getWidth() {
		return Game.TS;
	}

	@Override
	public int getHeight() {
		return Game.TS;
	}
}