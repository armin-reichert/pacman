package de.amr.games.pacman.actor.core;

import java.util.List;

import de.amr.games.pacman.actor.behavior.Steering;
import de.amr.games.pacman.model.Direction;
import de.amr.games.pacman.model.Tile;

/**
 * Implemented by entities that can move through a maze.
 * 
 * @author Armin Reichert
 */
public interface MazeMover extends MazeResident {

	/**
	 * Returns the current steering of this actor.
	 */
	Steering<?> steering();

	/**
	 * Walks through the maze, either inside the maze or through the teleporting space.
	 */
	void step();

	/**
	 * @return the maximum possible speed (in pixels/tick) for the current frame. The actual speed can
	 *         be lower to avoid moving into inaccessible tiles.
	 */
	float maxSpeed();

	/**
	 * @return current move direction
	 */
	Direction moveDir();

	void setMoveDir(Direction dir);

	/**
	 * @return next (=intended) move direction
	 */
	Direction nextDir();

	void setNextDir(Direction dir);

	/**
	 * @return if a new tile has been entered
	 */
	boolean enteredNewTile();

	void setEnteredNewTile();

	/**
	 * @return the current target tile
	 */
	Tile targetTile();

	void setTargetTile(Tile tile);

	/**
	 * @return the path to the current target tile (optionally computed)
	 */
	List<Tile> targetPath();

	void setTargetPath(List<Tile> path);

	/**
	 * @return if the steering should compute the complete path to the target tile, for example to
	 *         visualize the path.
	 */
	default boolean requireTargetPath() {
		return false;
	}

	/**
	 * @return if the actor is in teleporting state
	 */
	boolean isTeleporting();
	
	/**
	 * @return if the entity can move towards its current move direction
	 */
	boolean canMoveForward();

	/**
	 * @param dir
	 *              direction value (N, E, S, W)
	 * @return if the entity can enter the neighbor tile towards this direction
	 */
	boolean canCrossBorderTo(Direction dir);

	/**
	 * @param tile
	 *                   some tile
	 * @param neighbor
	 *                   neighbor the tile
	 * @return if the entity can move from the tile to the neighbor tile (might be state-dependent)
	 */
	boolean canMoveBetween(Tile tile, Tile neighbor);

	/**
	 * 
	 * @param n
	 *            some positive number
	 * @return the tile located <code>n</code> tiles away in the current move direction
	 */
	Tile tilesAhead(int n);
}