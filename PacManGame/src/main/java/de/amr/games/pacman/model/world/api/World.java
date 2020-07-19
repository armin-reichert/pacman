package de.amr.games.pacman.model.world.api;

/**
 * The Pac-Man game world is a territory where creatures can live and get their food.
 * 
 * @author Armin Reichert
 */
public interface World extends Territory, FoodSource {

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
}