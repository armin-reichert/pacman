package de.amr.games.pacman.actor.core;

import de.amr.games.pacman.actor.steering.core.Steering;
import de.amr.games.pacman.model.Direction;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;

/**
 * Implemented by entities that can move through a maze.
 * 
 * @author Armin Reichert
 */
public interface MazeMover {

	enum MoveState {
		MOVING, TELEPORTING;
	}

	/**
	 * @return the maze where this entity is moving
	 */
	Maze maze();

	/**
	 * @return tile where this entity is located
	 */
	Tile tile();

	/**
	 * Moves one step ans executes the afterStep code.
	 */
	void step();

	/**
	 * Returns the current steering of this actor.
	 */
	Steering steering();

	/**
	 * @return the maximum possible speed (in pixels/tick) for the next step. The
	 *         actual speed can be lower to avoid crossing borders into inaccessible
	 *         tiles.
	 */
	float maxSpeed();

	/**
	 * @return current move direction
	 */
	Direction moveDir();

	/**
	 * Sets the move direction.
	 * 
	 * @param dir move direction
	 */
	void setMoveDir(Direction dir);

	/**
	 * @return intended move direction
	 */
	Direction wishDir();

	/**
	 * Sets the intended move direction.
	 * 
	 * @param dir intended direction
	 */
	void setWishDir(Direction dir);

	/**
	 * @return if a new tile has been entered at the last step
	 */
	boolean enteredNewTile();

	/**
	 * @return the (optional) current target tile
	 */
	Tile targetTile();

	/**
	 * Sets the current target tile.
	 * 
	 * @param tile target tile
	 */
	void setTargetTile(Tile tile);

	/**
	 * @return if the actor is in teleporting state
	 */
	boolean isTeleporting();

	/**
	 * @param dir direction
	 * @return if the entity can enter the neighbor tile towards this direction
	 */
	boolean canCrossBorderTo(Direction dir);

	/**
	 * @param tile     some tile
	 * @param neighbor neighbor the tile
	 * @return if the entity can move from the tile to the neighbor tile (might be
	 *         state-dependent)
	 */
	boolean canMoveBetween(Tile tile, Tile neighbor);
}