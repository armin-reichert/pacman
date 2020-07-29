package de.amr.games.pacman.model.world.api;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Provides food-related functionality.
 * 
 * @author Armin Reichert
 */
public interface FoodSource {

	Stream<Food> food();

	Optional<Food> foodAt(Tile location);

	int totalFoodCount();

	void clearFood();

	void fillFood();

	void clearFood(Tile location);

	void fillFood(Tile location);

	void setFood(Food food, Tile location);
	
	default boolean containsFood(Tile location) {
		return foodAt(location).isPresent();
	}

	default boolean containsFood(Food food, Tile location) {
		return foodAt(location).filter(f -> f.equals(food)).isPresent();
	}

	boolean isEaten(Tile location);
}