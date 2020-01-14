package de.amr.games.pacman.actor.core;

import de.amr.games.pacman.model.Direction;
import de.amr.games.pacman.model.Tile;

/**
 * Implemented by entities that can move through a maze.
 * 
 * @author Armin Reichert
 */
public interface MazeMoving extends MazeResiding {

	enum MoveState {
		MOVING, TELEPORTING;
	}

	/**
	 * @return the current move direction
	 */
	Direction moveDir();

	/**
	 * Sets the move direction.
	 * 
	 * @param dir new move direction
	 */
	void setMoveDir(Direction dir);

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
	 * @return tells if the actor is in teleporting mode
	 */
	boolean isTeleporting();

	/**
	 * @param dir direction
	 * @return if the entity can move into the neighbor tile towards the given
	 *         direction
	 */
	boolean canCrossBorderTo(Direction dir);

	/**
	 * @param tile     some tile
	 * @param neighbor neighbor the tile
	 * @return tells if the entity can move from the given tile tile to the neighbor
	 *         tile (might be state-dependent)
	 */
	boolean canMoveBetween(Tile tile, Tile neighbor);

}