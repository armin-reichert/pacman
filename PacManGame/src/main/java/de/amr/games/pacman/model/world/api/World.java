package de.amr.games.pacman.model.world.api;

import java.util.stream.Stream;

/**
 * The Pac-Man game world is a territory and a habitat for the creatures.
 * 
 * @author Armin Reichert
 */
public interface World extends Territory, FoodContainer {

	/**
	 * Part of the word where the creatures live.
	 * 
	 * @return stream of tiles of habitat area
	 */
	Stream<Tile> habitatArea();

	/**
	 * @param creature a creature
	 * @return {@code true} if the creature is currently inside the world
	 */
	boolean contains(Lifeform creature);

	/**
	 * Brings the creature into the world.
	 * 
	 * @param creature a creature
	 */
	void include(Lifeform creature);

	/**
	 * Takes the creature out of the world.
	 * 
	 * @param creature a creature
	 */
	void exclude(Lifeform creature);

	/**
	 * Signals that the game level is changing. TODO: not sure if this belongs into the model
	 * 
	 * @param changing if the game level is changing
	 */
	void setChangingLevel(boolean changing);

	/**
	 * @return if the game level is just changing
	 */
	boolean isChangingLevel();

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