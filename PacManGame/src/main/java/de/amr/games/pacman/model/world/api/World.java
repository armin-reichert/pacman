package de.amr.games.pacman.model.world.api;

import de.amr.games.pacman.model.world.core.MovingGuy;

/**
 * The Pac-Man game world is a territory where creatures can live and get food.
 * 
 * @author Armin Reichert
 */
public interface World extends RectangularArea, Territory, FoodSource {

	/**
	 * Signals that the world is changing.
	 * 
	 * @param changing if the world is changing
	 */
	void setChanging(boolean changing);

	/**
	 * @return if the world is just changing
	 */
	boolean isChanging();

	/**
	 * Sets the world into "frozen" state where the creatures do not move and are not animated.
	 * 
	 * @param frozen if the world is frozen
	 */
	void setFrozen(boolean frozen);

	/**
	 * @return if the world is frozen
	 */
	boolean isFrozen();

	/**
	 * @param mover a mover
	 * @return {@code true} if the mover is currently included in this territory
	 */
	boolean contains(MovingGuy mover);

	/**
	 * Includes the mover into the territory.
	 * 
	 * @param mover a mover
	 */
	void include(MovingGuy mover);

	/**
	 * Excludes the mover from the territory.
	 * 
	 * @param mover a mover
	 */
	void exclude(MovingGuy mover);
}