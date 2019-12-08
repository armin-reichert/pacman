package de.amr.games.pacman.actor;

import static de.amr.games.pacman.model.PacManGame.TS;
import static java.lang.Math.round;

import de.amr.easy.game.entity.Entity;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;

/**
 * An entity residing in the maze.
 * 
 * @author Armin Reichert
 */
public abstract class MazeResident extends Entity {

	public final Maze maze;

	public MazeResident(Maze maze) {
		this.maze = maze;
		tf.setWidth(TS);
		tf.setHeight(TS);
	}

	/**
	 * @param other
	 *                other maze resident
	 * @return squared Euclidean distance to the other maze resident in tile coordinates
	 */
	public int tileDistanceSq(MazeMover other) {
		return Tile.distanceSq(tile(), other.tile());
	}

	/**
	 * @return maze column where the center of the collision box is located
	 */
	public int col() {
		return round(tf.getCenter().x) / TS;
	}

	/**
	 * @return maze row where the center of the collision box is located
	 */
	public int row() {
		return round(tf.getCenter().y) / TS;
	}

	/**
	 * @return maze tile where the center of the collision box is located
	 */
	public Tile tile() {
		return maze.tileAt(col(), row());
	}

	/**
	 * Places this maze mover at the given tile, optionally with some pixel offset.
	 * 
	 * @param tile
	 *                  the tile where this maze mover is placed
	 * @param xOffset
	 *                  pixel offset in x-direction
	 * @param yOffset
	 *                  pixel offset in y-direction
	 */
	public void placeAtTile(Tile tile, float xOffset, float yOffset) {
		tf.setPosition(tile.col * TS + xOffset, tile.row * TS + yOffset);
	}
}