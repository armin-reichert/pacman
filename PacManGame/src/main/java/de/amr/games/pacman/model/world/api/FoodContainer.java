package de.amr.games.pacman.model.world.api;

import java.util.Optional;

/**
 * Provides food-related functionality.
 * 
 * @author Armin Reichert
 */
public interface FoodContainer {

	int totalFoodCount();

	void clearFood();

	void fillFood();

	void clearFood(Tile tile);

	void fillFood(Tile tile);

	boolean containsFood(Tile tile);

	boolean containsSimplePellet(Tile tile);

	boolean containsEnergizer(Tile tile);

	boolean didContainFood(Tile tile);

	void setBonus(Bonus bonus);

	Optional<Bonus> getBonus();
}