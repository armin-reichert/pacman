/*
MIT License

Copyright (c) 2019 Armin Reichert

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package de.amr.games.pacmanfsm.model.world.core;

import de.amr.easy.game.entity.Entity;
import de.amr.games.pacmanfsm.lib.Tile;
import de.amr.games.pacmanfsm.model.world.api.TiledWorld;

/**
 * An entity in a tile-based world.
 * 
 * @author Armin Reichert
 */
public abstract class TileWorldEntity extends Entity {

	public final TiledWorld world;

	protected TileWorldEntity(TiledWorld world) {
		this.world = world;
	}

	/**
	 * The tile location is defined as the tile containing the center of the guy's body.
	 * 
	 * @return tile location of this guy
	 */
	public Tile tile() {
		return Tile.at(col(), row());
	}

	/**
	 * The current tile column (x-coordinate).
	 * 
	 * @return column index of current tile
	 */
	public int col() {
		float centerX = tf.x + tf.width / 2;
		return (int) (centerX >= 0 ? centerX / Tile.TS : Math.floor(centerX / Tile.TS));
	}

	/**
	 * The current tile row (y-coordinate).
	 * 
	 * @return row index of current tile
	 */
	public int row() {
		float centerY = tf.y + tf.height / 2;
		return (int) (centerY >= 0 ? centerY / Tile.TS : Math.floor(centerY / Tile.TS));
	}

	/**
	 * The deviation from the current tile's x-position from the center of its current tile, a value between 0 and
	 * Tile.SIZE.
	 * 
	 * @return the horizontal tile offset
	 */
	public float tileOffsetX() {
		return (tf.x + tf.width / 2) - col() * Tile.TS;
	}

	/**
	 * The deviation from the current tile's y-position from the center of its current tile, a value between 0 and
	 * Tile.SIZE.
	 * 
	 * @return the vertical tile offset
	 */
	public float tileOffsetY() {
		return (tf.y + tf.height / 2) - row() * Tile.TS;
	}

	/**
	 * Places this guy at the given tile location.
	 * 
	 * @param tile tile location
	 * @param dx   additional pixels in x-direction
	 * @param dy   additional pixels in y-direction
	 */
	public void placeAt(Tile tile, float dx, float dy) {
		tf.setPosition(tile.x() + dx, tile.y() + dy);
	}

	/**
	 * Euclidean distance (in tiles) between this and the other guy.
	 * 
	 * @param other other guy
	 * @return Euclidean distance measured in tiles
	 */
	public double tileDistance(TileWorldEntity other) {
		return tile().distance(other.tile());
	}
}