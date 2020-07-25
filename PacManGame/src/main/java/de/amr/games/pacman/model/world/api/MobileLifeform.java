package de.amr.games.pacman.model.world.api;

import java.util.stream.Stream;

/**
 * A life form that can move through the world.
 * 
 * @author Armin Reichert
 */
public interface MobileLifeform extends Lifeform {

	/**
	 * @return the current move direction
	 */
	Direction moveDir();

	/**
	 * Sets the move direction.
	 * 
	 * @param dir the wanted move direction
	 */
	void setMoveDir(Direction dir);

	/**
	 * @param dirs directions
	 * @return if the lifeform is moving to any of the given directions
	 */
	default boolean isMoving(Direction... dirs) {
		return Stream.of(dirs).anyMatch(dir -> dir == moveDir());
	}

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
	 * @return tells if this lifeform is moving exactly along the grid
	 */
	boolean requiresAlignment();

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