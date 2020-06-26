package de.amr.games.pacman.controller.actor;

import de.amr.games.pacman.model.Direction;
import de.amr.games.pacman.model.PacManWorld;
import de.amr.games.pacman.model.Tile;

/**
 * Implemented by entities that can move through a maze.
 * 
 * @author Armin Reichert
 */
public interface MazeMover {

	/**
	 * @return tile position
	 */
	Tile tile();

	/**
	 * Euclidean distance (in tiles) between this and the other entity.
	 * 
	 * @param other other entity
	 * @return Euclidean distance measured in tiles
	 */
	default double distance(MazeMover other) {
		return tile().distance(other.tile());
	}

	/**
	 * @return the maze where this mover is located
	 */
	PacManWorld world();

	/**
	 * @return the current move direction
	 */
	Direction moveDir();

	/**
	 * @return the wanted move direction
	 */
	Direction wishDir();

	/**
	 * Sets the wanted move direction.
	 * 
	 * @param dir the wanted move direction
	 */
	void setWishDir(Direction dir);

	/**
	 * @return tells if a new tile has been entered with the previous move
	 */
	boolean enteredNewTile();

	/**
	 * @return the (optional) target tile
	 */
	Tile targetTile();

	/**
	 * Sets the target tile.
	 * 
	 * @param tile target tile of this entity
	 */
	void setTargetTile(Tile tile);

	/**
	 * @param dir direction
	 * @return if the entity can move into the neighbor tile towards the given direction
	 */
	boolean canCrossBorderTo(Direction dir);

	/**
	 * @param tile     some tile
	 * @param neighbor neighbor the tile
	 * @return tells if the entity can move from the given tile tile to the neighbor tile (might be
	 *         state-dependent)
	 */
	boolean canMoveBetween(Tile tile, Tile neighbor);

	/**
	 * Forces the entity to move to the given direction.
	 * 
	 * @param dir direction
	 */
	void forceMoving(Direction dir);

	/**
	 * Forces the entity to reverse its current move direction.
	 */
	default void reverseDirection() {
		forceMoving(moveDir().opposite());
	}
}